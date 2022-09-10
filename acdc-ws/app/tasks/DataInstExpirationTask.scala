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

class DataInstExpirationTask @Inject()(actorSystem: ActorSystem, dbService: DatabaseService)(implicit
  ec: ExecutionContext
) extends Logging {

  val refreshDelay: FiniteDuration = 10.second
  val hostName = (InetAddress.getLocalHost.getHostName + UUID.randomUUID().toString).take(256)
  def db = dbService.db

  def refresh(): Unit = {
    actorSystem.scheduler.scheduleOnce(refreshDelay) {
      val latest = db.sync(ExpirationTaskQuery.getLatest())
      if (latest.forall(_.isBefore(LocalDateTime.now().minusSeconds(30)))) {
        db.sync(ExpirationTaskQuery.insert(hostName))
        val count = db.sync(DatasetInstanceQuery.expire())
        logger.warn(s"Expired $count records from dataset_instance ...")
      }

      refresh()
    }
  }

  refresh()

}
