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

  def deleteDatasetMappings(dataset: String) =
    DatasetInstanceTable().filter(_.dataset === dataset).map(_.dataset).update(None)

  case class ForName(name: String) {

    def table = DatasetInstanceTable().filter(_.name === name)

    def get() = table.result.headOption

    def delete() = table.delete

    def insert(location: String) =
      DatasetInstanceTable() += DatasetInstanceTable.R(name, location, LocalDateTime.now())

    def create(location: String)(implicit ec: ExecutionContext) = (
      for {
        di <- get()
        r <- if (di.isEmpty) insert(location) else DBIO.successful(0)
      } yield r
    ).transactionally

    def mapTo(dataset: String) = table.map(_.dataset).update(Option(dataset))

    def removeFrom(dataset: String) = table.map(_.dataset).update(None)

  }

}
