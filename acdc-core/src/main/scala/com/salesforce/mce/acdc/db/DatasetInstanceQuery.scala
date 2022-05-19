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

  def list(prefix: String) =
    DatasetInstanceTable().filter(_.name.like(s"${prefix}%")).map(_.name).result

  def search(term: String) =
    DatasetInstanceTable().filter(_.name.like(s"%${term}%")).map(_.name).result

  def forDataset(dataset: String) = DatasetInstanceTable().filter(_.dataset === dataset).result

  case class ForInstance(dataset: String, name: String) {

    def table = DatasetInstanceTable().filter(_.name === name)

    def get() = table.result.headOption

    def delete() = table.delete

    def insert(location: String): DBIO[DatasetInstanceTable.R] = {
      val currentTime = LocalDateTime.now()
      (DatasetInstanceTable() returning DatasetInstanceTable()) += DatasetInstanceTable.R(
        dataset,
        name,
        location,
        false,
        currentTime,
        currentTime
      )
    }

    def create(location: String)(implicit
      ec: ExecutionContext
    ): DBIO[Either[DatasetInstanceTable.R, DatasetInstanceTable.R]] = get().flatMap {
      case Some(r) => DBIO.successful(Left(r))
      case None => insert(location).map(Right(_))
    }.transactionally

    def setActivation(isActive: Boolean) =
      table.map(r => (r.isActive, r.updatedAt)).update((isActive, LocalDateTime.now()))

  }

}
