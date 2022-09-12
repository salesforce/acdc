/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package tasks

import java.net.InetAddress
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor.ActorSystem
import play.api.Logging
import services.DatabaseService

import com.salesforce.mce.acdc.db.{DatasetInstanceQuery, ExpirationTaskQuery}
import utils.DbConfig

class DataInstExpirationTask @Inject()(
  actorSystem: ActorSystem,
  dbService: DatabaseService,
  dbConfig: DbConfig
)(implicit
  ec: ExecutionContext
) extends Logging {

  val refreshDelay: FiniteDuration = 12.hour
  val hostName = (UUID.randomUUID().toString + "_" + InetAddress.getLocalHost.getHostName).take(256)
  def db = dbService.db

  def refresh(): Unit = {
    actorSystem.scheduler.scheduleOnce(refreshDelay) {
      val latest = db.sync(ExpirationTaskQuery.getLatest())
      if (latest.forall(_.isBefore(LocalDateTime.now().minusDays(1)))) {
        db.sync(ExpirationTaskQuery.insert(hostName))
        val count = db.sync(DatasetInstanceQuery.expire(dbConfig.ttl))
        logger.info(s"Expired $count records from dataset_instance ...")
      }

      refresh()
    }
  }

  refresh()

}
