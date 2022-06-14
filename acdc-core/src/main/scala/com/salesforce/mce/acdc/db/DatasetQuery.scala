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

  object OrderColumn extends Enumeration {

    val CreatedAt = Value("created_at")
    val UpdatedAt = Value("updated_at")
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
    order: OrderColumn.Value,
    limit: Int,
    offset: Int
  ): DBIO[Seq[DatasetTable.R]] = {
    val ordering = order match {
      case OrderColumn.CreatedAt => t: DatasetTable => t.createdAt
      case OrderColumn.UpdatedAt => t: DatasetTable => t.updatedAt
      case _ => throw new MatchError("unexpected order")
    }
    DatasetTable()
      .filter(_.name.like(s"$like"))
      .sortBy(ordering(_).desc)
      .drop(offset)
      .take(limit)
      .result
  }

}
