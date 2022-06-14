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

object DatasetQuery {

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

  def now() = LocalDateTime.now()

  case class ForName(name: String) {

    def table = DatasetTable().filter(_.name === name)

    def get(): DBIO[Option[DatasetTable.R]] = table.result.headOption

    def delete()(implicit ec: ExecutionContext) = {
      DatasetInstanceQuery.forDataset(name).headOption.flatMap {
        case Some(_) =>
          DBIO.successful(-1)
        case None =>
          table.delete
      }
    }

    def insert(meta: Option[String]): DBIO[DatasetTable.R] = {
      val currentTime = now()
      (DatasetTable() returning DatasetTable()) += DatasetTable.R(
        name,
        currentTime,
        currentTime,
        meta
      )
    }

    def create(meta: Option[String])(implicit
      ec: ExecutionContext
    ): DBIO[Either[DatasetTable.R, DatasetTable.R]] = get().flatMap {
      case Some(r) => DBIO.successful(Left(r))
      case None => insert(meta).map(Right(_))
    }.transactionally

    def update(
      newName: String
    )(implicit ec: ExecutionContext): DBIO[Either[DatasetTable.R, Int]] =
      ForName(newName)
        .get()
        .flatMap {
          case Some(r) => DBIO.successful(Left(r))
          case None => table.map(r => (r.name, r.updatedAt)).update((newName, now())).map(Right(_))
        }
        .transactionally

  }

  def filter(
    like: String,
    orderBy: OrderBy,
    order: Order,
    limit: Int,
    offset: Int
  ): DBIO[Seq[DatasetTable.R]] = {
    val sortByColumn = (orderBy, order) match {
      case (OrderBy.CreatedAt, Order.Desc) =>
        t: DatasetTable => t.createdAt.desc
      case (OrderBy.CreatedAt, Order.Asc) =>
        t: DatasetTable => t.createdAt.asc
      case (OrderBy.UpdatedAt, Order.Desc) =>
        t: DatasetTable => t.updatedAt.desc
      case (OrderBy.UpdatedAt, Order.Asc) =>
        t: DatasetTable => t.updatedAt.asc
    }
    DatasetTable()
      .filter(_.name.like(s"$like"))
      .sortBy(sortByColumn)
      .drop(offset)
      .take(limit)
      .result
  }

}
