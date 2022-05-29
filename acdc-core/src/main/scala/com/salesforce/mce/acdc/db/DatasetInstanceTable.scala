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

  def dataset = column[String]("dataset", O.SqlType("VARCHAR(256)"))

  def name = column[String]("name", O.SqlType("VARCHAR(256)"))

  def createdAt = column[LocalDateTime]("created_at")

  def updatedAt = column[LocalDateTime]("updated_at")

  // use 2048 for max url length
  def location = column[String]("location", O.SqlType("VARCHAR(2048)"))

  def isActive = column[Boolean]("is_active")

  def meta = column[Option[String]]("meta", O.SqlType("TEXT"))

  def pk = primaryKey("pk_dataset_instance", (dataset, name))

  def datasetTable = foreignKey("fk_dataset_dataset", dataset, TableQuery[DatasetTable])(
    _.name,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Restrict
  )

  override def * =
    (dataset, name, createdAt, updatedAt, location, isActive, meta).mapTo[DatasetInstanceTable.R]

}

object DatasetInstanceTable {

  def apply() = TableQuery[DatasetInstanceTable]

  case class R(
    dataset: String,
    name: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    location: String,
    isActive: Boolean,
    meta: Option[String]
  )

}
