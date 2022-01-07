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

  def now() = LocalDateTime.now()

  case class ForName(name: String) {

    def table = DatasetTable().filter(_.name === name)

    def get() = table.result.headOption

    def delete() = table.delete

    def insert() = DatasetTable() += DatasetTable.R(name, now(), now())

    def create()(implicit ec: ExecutionContext) = (
      for {
        ds <- get()
        r <- if (ds.isEmpty) insert() else DBIO.successful(0)
      } yield r
    ).transactionally

    def update(newName: String)(implicit ec: ExecutionContext) = (
      for {
        ds <- get()
        r <-
          if (ds.nonEmpty) DBIO.successful(-1)
          else table.map(r => (r.name, r.updatedAt)).update((newName, now()))
      } yield r
    ).transactionally

  }

}
