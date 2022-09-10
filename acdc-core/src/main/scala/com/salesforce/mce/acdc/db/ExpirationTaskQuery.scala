/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.mce.acdc.db

import java.time.LocalDateTime

import slick.jdbc.PostgresProfile.api._

object ExpirationTaskQuery {

  def table = ExpirationTaskTable()

  def getLatest() = table.map(_.timestamp).max.result

  def insert(taskRunnerName: String) = {
    val record = ExpirationTaskTable.R(taskRunnerName, LocalDateTime.now())
    ExpirationTaskTable().returning(ExpirationTaskTable()) += record
  }

}
