package tasks

import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import play.api.Logging

import com.salesforce.mce.acdc.db.{
  AcdcDatabase,
  DatasetInstanceQuery,
  DatasetLineageQuery,
  DatasetQuery
}
import services.DatabaseService
import utils.DbConfig

class DataCountTask @Inject() (
  actorSystem: ActorSystem,
  dbService: DatabaseService,
  dbConfig: DbConfig
)(implicit
  ec: ExecutionContext
) extends Logging {

  val countFrequency: Int = dbConfig.countTaskFrequencyMinute
  def db: AcdcDatabase = dbService.db

  def refresh(): Unit = {
    actorSystem.scheduler.scheduleOnce(countFrequency.minutes) {
      val datasetCount = db.sync(DatasetQuery.count())
      val datasetInstanceCount = db.sync(DatasetInstanceQuery.count())
      val datasetLineageCount = db.sync(DatasetLineageQuery.count())

      logger.debug(s"Counted $datasetCount records from dataset ...")
      logger.debug(s"Counted $datasetInstanceCount records from dataset_instance ...")
      logger.debug(s"Counted $datasetLineageCount records from dataset_lineage ...")

      // TODO: log via extending kineticpulse PrometheusMetric for metrics scraping

      refresh()
    }
  }

  if (countFrequency > 0) {
    refresh()
  } else {
    logger.info("data count frequency is not a positive integer, data count task skipped.")
  }
}
