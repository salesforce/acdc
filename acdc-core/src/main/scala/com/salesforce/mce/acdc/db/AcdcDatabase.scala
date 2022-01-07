/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.db

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import com.typesafe.config.Config
import slick.jdbc.PostgresProfile.api._

import com.salesforce.mce.acdc.AcdcSettings

class AcdcDatabase(conf: Config) {

  val connection: Database = Database.forConfig("slick", conf)

  def async[T](dbio: DBIO[T]): Future[T] = connection.run(dbio)

  def sync[T](dbio: DBIO[T]): T = Await.result(async(dbio), 1.minute)

  def sync[T](dbio: DBIO[T], duration: Duration): T = Await.result(async(dbio), duration)

}

object AcdcDatabase {

  def apply(): AcdcDatabase = apply(AcdcSettings())

  def apply(settings: AcdcSettings): AcdcDatabase = new AcdcDatabase(settings.slickDatabaseConf)

}
