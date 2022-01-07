/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.db

import java.time.LocalDateTime

import slick.jdbc.PostgresProfile.api._

class DatasetInstanceTable(tag: Tag)
    extends Table[DatasetInstanceTable.R](tag, "dataset_instance") {

  def name = column[String]("name", O.SqlType("VARCHAR(256)"), O.PrimaryKey)

  // use 2048 for max url length
  def location = column[String]("location", O.SqlType("VARCHAR(2048)"))

  def dataset = column[Option[String]]("dataset", O.SqlType("VARCHAR(256)"))

  def datasetTable = foreignKey("fk_dataset_dataset", dataset, TableQuery[DatasetTable])(
    _.name.?,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.SetNull
  )

  def createdAt = column[LocalDateTime]("created_at")

  override def * = (name, location, createdAt).mapTo[DatasetInstanceTable.R]

}

object DatasetInstanceTable {

  def apply() = TableQuery[DatasetInstanceTable]

  case class R(name: String, location: String, createdAt: LocalDateTime)

}
