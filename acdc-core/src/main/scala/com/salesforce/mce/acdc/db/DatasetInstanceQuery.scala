/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.db

import java.time.LocalDateTime

import scala.concurrent.ExecutionContext

import slick.jdbc.PostgresProfile.api._

object DatasetInstanceQuery {

  sealed trait OrderBy
  object OrderBy {
    case object CreatedAt extends OrderBy
    case object UpdatedAt extends OrderBy
  }

  sealed trait Order
  object Order {
    case object Asc extends Order
    case object Desc extends Order
  }

  def list(prefix: String) =
    DatasetInstanceTable().filter(_.name.like(s"${prefix}%")).map(_.name).result

  def search(term: String) =
    DatasetInstanceTable().filter(_.name.like(s"%${term}%")).map(_.name).result

  def forDataset(dataset: String) = DatasetInstanceTable().filter(_.dataset === dataset).result

  def filter(
    dataset: Option[String],
    like: Option[String],
    orderBy: OrderBy,
    order: Order,
    limit: Int,
    offset: Int
  ): DBIO[Seq[DatasetInstanceTable.R]] = {
    val sortByColumn = (orderBy, order) match {
      case (OrderBy.CreatedAt, Order.Desc) =>
        t: DatasetInstanceTable => t.createdAt.desc
      case (OrderBy.CreatedAt, Order.Asc) =>
        t: DatasetInstanceTable => t.createdAt.asc
      case (OrderBy.UpdatedAt, Order.Desc) =>
        t: DatasetInstanceTable => t.updatedAt.desc
      case (OrderBy.UpdatedAt, Order.Asc) =>
        t: DatasetInstanceTable => t.updatedAt.asc
    }

    val filteredByDataset = dataset
      .foldLeft[Query[DatasetInstanceTable, DatasetInstanceTable.R, Seq]](DatasetInstanceTable())(
        (t, d) => t.filter(r => r.dataset === d)
      )

    like.foldLeft(filteredByDataset)((t, l) => t.filter(r => r.name.like(l)))
      .sortBy(sortByColumn)
      .drop(offset)
      .take(limit)
      .result
  }

  def expire(ttl: Int) = {
    DatasetInstanceTable().filter(_.updatedAt < LocalDateTime.now().minusDays(ttl)).delete
  }

  case class ForInstance(dataset: String, name: String) {

    def table = DatasetInstanceTable().filter(r => r.dataset === dataset && r.name === name)

    def get() = table.result.headOption

    def delete() = table.delete

    def insert(location: String, meta: Option[String]): DBIO[DatasetInstanceTable.R] = {
      val currentTime = LocalDateTime.now()
      (DatasetInstanceTable() returning DatasetInstanceTable()) += DatasetInstanceTable.R(
        dataset,
        name,
        currentTime,
        currentTime,
        location,
        false,
        meta
      )
    }

    def create(location: String, meta: Option[String])(implicit
      ec: ExecutionContext
    ): DBIO[Either[DatasetInstanceTable.R, DatasetInstanceTable.R]] = get().flatMap {
      case Some(r) => DBIO.successful(Left(r))
      case None => insert(location, meta).map(Right(_))
    }.transactionally

    def setActivation(isActive: Boolean) =
      table.map(r => (r.isActive, r.updatedAt)).update((isActive, LocalDateTime.now()))

  }

}
