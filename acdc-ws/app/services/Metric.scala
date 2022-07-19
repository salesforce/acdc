package services

import com.typesafe.config.ConfigFactory
import io.prometheus.client._
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import play.api.Configuration
import play.api.mvc.RequestHeader

import java.io.StringWriter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait Metric {
  private lazy val config = new Configuration(ConfigFactory.load())

  private val disableMetrics = config.getOptional[String]("acdc.metrics.endpoint").getOrElse("").isEmpty

  private val bypassPaths = config.getOptional[Seq[String]]("acdc.metrics.bypass.paths") match {
    case Some(paths) => paths.toSet
    case _ => Set[String]()
  }

  private val STATIC_PATH_MARKERS = Seq("instance", "dataset", "lineage", "__metrics", "__status")

  def parseRequest(request: RequestHeader): Option[(String, String)] = {
    if ( checkPathIsDisabled(request.path) || checkMetricIsDisabled )
      None
    else {
      val (staticPath, args) = request.path
        .split("/")
        .filter(_.nonEmpty)
        .foldLeft((List[String](), List[String]())) {
          case ( (Nil, l2), token) => (List(token), l2)
          case ( (h::t, l2), token ) => if (STATIC_PATH_MARKERS.contains(h)) (h::t, token::l2)
          else (token::h::t, l2)
        }
      Some((staticPath.reverse.mkString("-") , args.reverse.mkString("-")))
    }
  }

  def checkMetricIsDisabled: Boolean = { disableMetrics }

  def checkPathIsDisabled(path: String): Boolean = { bypassPaths.contains(path) }

  def incrementStatusCount(status: String) : Unit

  def startApiTimer(labels: String*): () => Unit

  def collect: Future[String]

  def clear(): Unit

}


@Singleton
class PrometheusMetric @Inject() (implicit ec: ExecutionContext) extends Metric {

  DefaultExports.initialize()

  private val httpStatusCount: Counter = Counter.build
    .name("http_requests_total")
    .help("Total HTTP Requests Count")
    .labelNames("status")
    .register

  private val apiLatency: Summary = Summary
    .build()
    .name("apiLatencySummary")
    .labelNames(Seq("path", "arguments", "method") : _*)
    .help("Profile API response time summary")
    .quantile(0.5d, 0.001d)
    .quantile(0.95d, 0.001d)
    .quantile(0.99d, 0.001d)
    .register

  override def incrementStatusCount(status: String) : Unit = {
    httpStatusCount.labels(status).inc()
  }

  override def startApiTimer(labels: String*): () => Unit  = {
    val timer = apiLatency.labels(labels: _*).startTimer()
    val callback = () => timer.close
    callback
  }

  // Get metrics from the local prometheus collector default registry
  override def collect: Future[String] = Future {
    val writer = new StringWriter()
    TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
    writer.toString
  }

  override def clear(): Unit = { apiLatency.clear() }

}
