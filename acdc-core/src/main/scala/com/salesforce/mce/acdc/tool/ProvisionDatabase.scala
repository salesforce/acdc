/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.tool

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

import slick.jdbc.PostgresProfile.api._

import com.salesforce.mce.acdc.db._

object ProvisionDatabase extends App {

  val prodMode: Boolean = Try(args(0)).map(_.toBoolean).getOrElse(false)

  val schema = DatasetTable().schema ++ DatasetInstanceTable().schema ++
    DatasetLineageTable().schema

  val db = AcdcDatabase()

  println("executing the following statements...")
  // note that dropIfExists does not work properly because it need to perform alter table to drop
  // the constraint first
  schema.drop.statements.foreach(println)
  if (prodMode)
    Await.result(
      db.connection.run(DBIO.seq(schema.drop)),
      2.minutes
    )

  println("executing the following statements...")
  // note that we need to use create rather than createIfNotExists because there is a bug in slick
  // that createIfNotExists does not add foreign key
  schema.create.statements.foreach(println)
  if (prodMode)
    Await.result(
      db.connection.run(DBIO.seq(schema.create)),
      2.minutes
    )

}
