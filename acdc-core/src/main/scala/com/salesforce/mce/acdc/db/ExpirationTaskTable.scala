/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.db

import java.time.LocalDateTime

import slick.jdbc.PostgresProfile.api._

class ExpirationTaskTable(tag: Tag)
  extends Table[ExpirationTaskTable.R](tag, "expiration_task") {

  def taskRunnerName = column[String]("task_runner_name", O.SqlType("VARCHAR(256)"))

  def timestamp = column[LocalDateTime]("timestamp")

  def pk = primaryKey("pk_expiration_task", (taskRunnerName, timestamp))

  override def * = (taskRunnerName, timestamp).mapTo[ExpirationTaskTable.R]

}

object ExpirationTaskTable {

  def apply() = TableQuery[ExpirationTaskTable]

  case class R(
    taskRunnerName: String,
    timestamp: LocalDateTime
  )

}
