package services

import com.typesafe.config.ConfigFactory
import io.prometheus.client._
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import play.api.Configuration

import java.io.StringWriter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object Metrics {

  val DefaultBucketValues: Array[Double] = Array(
    .005,
    .010,
    .025,
    .050,
    .100,
    .250,
    .500,
    1.0,
    2.5,
    5.0,
    10.0,
    15.0,
    60.0,
    300.0,
    1200.0,
    Double.PositiveInfinity
  )

  val httpDurationSeconds: Histogram = Histogram.build
    .buckets(DefaultBucketValues: _*)
    .name("http_request_duration_seconds")
    .help("Http Request Duration in Seconds")
    .register

  val httpTotalRequests: Counter = Counter.build
    .name("http_requests_total")
    .help("Total HTTP Requests Count")
    .labelNames("status")
    .register

  val API_LATENCY_LABELS = Seq("path", "arguments", "method")
  val apiLatencySummary: Summary = Summary
    .build()
    .name("apiLatencySummary")
    .labelNames(API_LATENCY_LABELS : _*)
    .help("Profile API response time summary")
    .quantile(0.5d, 0.001d)
    .quantile(0.95d, 0.001d)
    .quantile(0.99d, 0.001d)
    .register

  // exclude from clearMetrics() due to constant higher frequency
  val runtimeFreeMemory = Gauge
    .build()
    .name("runtimeFreeMemory")
    .help("runtime free memory")
    .register

  def clearMetrics(): Unit = {
    httpDurationSeconds.clear()
    apiLatencySummary.clear()
  }
}

trait MetricReporter {

  def metrics: Future[String]
  def checkMetricIsDisabled: Boolean
  def checkPathIsDisabled(path: String): Boolean
  val httpDurationSeconds: Histogram
  val httpTotalRequests: Counter
}

@Singleton
class MetricReporterImpl @Inject() (implicit ec: ExecutionContext) extends MetricReporter {

  // Initialize the default jmx stats as metrics for prometheus
  DefaultExports.initialize()

  private lazy val config = new Configuration(ConfigFactory.load())

  private val bypassPaths = config.getOptional[Seq[String]]("acdc.metrics.bypass.paths") match {
    case Some(paths) => paths.toSet
    case _ => Set[String]()
  }

  private val disableMetrics = config.getOptional[String]("acdc.metrics.endpoint").getOrElse("").isEmpty

  // Get metrics from the local prometheus collector default registry
  override def metrics: Future[String] = Future {
    val writer = new StringWriter()
    TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
    writer.toString
  }

  override def checkMetricIsDisabled: Boolean = {
    disableMetrics
  }

  override def checkPathIsDisabled(path: String): Boolean = {
    bypassPaths.contains(path)
  }

  override val httpDurationSeconds: Histogram = Metrics.httpDurationSeconds
  override val httpTotalRequests: Counter = Metrics.httpTotalRequests
}
