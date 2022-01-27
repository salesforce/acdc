/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.tool

import scala.concurrent.Await
import scala.concurrent.duration._

import slick.jdbc.PostgresProfile.api._

import com.salesforce.mce.acdc.db._

object ProvisionDatabase extends App {

  val schema = DatasetTable().schema ++ DatasetInstanceTable().schema ++
    DatasetLineageTable().schema

  val db = AcdcDatabase()

  checkFirstTimeProvision(db)

  println("executing the following statements...")
  schema.dropIfExistsStatements.foreach(println)
  Await.result(
    db.connection.run(DBIO.seq(schema.dropIfExists)),
    2.minutes
  )

  println("executing the following statements...")
  schema.createIfNotExistsStatements.foreach(println)
  Await.result(
    db.connection.run(DBIO.seq(schema.createIfNotExists)),
    2.minutes
  )

  /**
   * corner case for 1st time creation of schema, when the drop would give
   * an error in a step before drop table dataset_lineage to
   * alter (non-existing) table drop constraint for the primary key "pk_dataset_lineage"
   * @param db  AcdcDatabase
   */
  private def checkFirstTimeProvision(db: AcdcDatabase): Unit = {

    def datasetLineageOption() = {
      lazy val public = "public"
      lazy val datasetLineage = DatasetLineageTable().baseTableRow.tableName
      sql"select tablename from pg_tables where schemaname = $public and tablename = $datasetLineage"
        .as[String]
        .headOption
    }

    Await.result(
      db.connection.run(datasetLineageOption()),
      2.minutes
    ) match {
      case None =>
        println("First time to provision database...")
        // create dataset_lineage to avoid error in the regular idempotent steps (schema dropIfExists, createIfNotExists)
        DatasetLineageTable().schema.createIfNotExistsStatements.foreach(println)
        Await.result(
          db.connection.run(DBIO.seq(DatasetLineageTable().schema.createIfNotExists)),
          2.minutes
        )
      case Some(_) =>
        // no action necessary
        println(s"Provision database re-run...")
    }
  }

}
