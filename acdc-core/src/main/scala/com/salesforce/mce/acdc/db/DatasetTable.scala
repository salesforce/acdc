/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.db

import java.time.LocalDateTime

import slick.jdbc.PostgresProfile.api._

class DatasetTable(tag: Tag) extends Table[DatasetTable.R](tag, "dataset") {

  def name = column[String]("name", O.SqlType("VARCHAR(256)"), O.PrimaryKey)

  def createdAt = column[LocalDateTime]("created_at")

  def updatedAt = column[LocalDateTime]("updated_at")

  def meta = column[Option[String]]("meta", O.SqlType("TEXT"))

  override def * = (name, createdAt, updatedAt, meta).mapTo[DatasetTable.R]

}

object DatasetTable {

  def apply() = TableQuery[DatasetTable]

  case class R(
    name: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    meta: Option[String]
  )

}
