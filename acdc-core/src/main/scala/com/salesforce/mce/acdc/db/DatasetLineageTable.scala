/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.db

import slick.jdbc.PostgresProfile.api._

class DatasetLineageTable(tag: Tag) extends Table[DatasetLineageTable.R](tag, "dataset_lineage") {

  def fromDataset = column[String]("from_dataset", O.SqlType("VARCHAR(256)"))

  def toDataset = column[String]("to_dataset", O.SqlType("VARCHAR(256)"))

  def pk = primaryKey("pk_dataset_lineage", (fromDataset, toDataset))

  def fromDatasetTable =
    foreignKey("fk_dataset_lineage_from_dataset", fromDataset, TableQuery[DatasetTable])(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      // source table should not be deleted unless it's child table are also deleted
      onDelete = ForeignKeyAction.Restrict
    )

  def toDatasetTable =
    foreignKey("fk_dataset_lineage_to_dataset", fromDataset, TableQuery[DatasetTable])(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade
    )

  override def * = (fromDataset, toDataset).mapTo[DatasetLineageTable.R]

}

object DatasetLineageTable {

  def apply() = TableQuery[DatasetLineageTable]

  case class R(fromDataset: String, toDataset: String)

}
