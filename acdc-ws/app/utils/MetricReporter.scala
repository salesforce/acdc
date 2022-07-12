package utils

import io.prometheus.client._
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports

import java.io.StringWriter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

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
    .help("Total HTTP Requests Count")
    .register

  val httpTotalRequests: Counter = Counter.build
    .name("http_requests_total")
    .help("Total HTTP Requests Count")
    .labelNames("status")
    .register

  val apiLatencySummary: Summary = Summary
    .build()
    .name("apiLatencySummary")
    .labelNames("path", "arguments", "method")
    .help("Profile API response time summary")
    .register

  val apiLatencyGauge: Gauge = Gauge
    .build()
    .name("apiLatencyGauge")
    .labelNames("path", "arguments", "method")
    .help("Profile response time in seconds")
    .register

  // exclude from clearMetrics() due to constant higher frequency
  val runtimeFreeMemory = Gauge
    .build()
    .name("runtimeFreeMemory")
    .help("runtime free memory")
    .register

  def setSumAvgToGauge(summary: Summary, gauge: Gauge): Unit = {
    for {
      samples <- summary.collect.asScala.toList.map{_.samples.asScala.toList}
      values: List[String] <- samples.map(_.labelValues.asScala.toList)
    } yield {
      val (sum, count) = (summary.labels(values : _*).get().sum, summary.labels(values: _*).get().count)
      if (count > 0) gauge.labels(values: _*).set(sum * 1d / count)
    }
  }

  def clearMetrics(): Unit = {
    httpDurationSeconds.clear()
    apiLatencySummary.clear()
    apiLatencyGauge.clear()
  }
}

trait MetricReporter {

  def metrics: Future[String]
  val httpDurationSeconds: Histogram
  val httpTotalRequests: Counter
}

@Singleton
class MetricReporterImpl @Inject() (implicit ec: ExecutionContext) extends MetricReporter {

  // Initialize the default jmx stats as metrics for prometheus
  DefaultExports.initialize()

  // Get metrics from the local prometheus collector default registry
  override def metrics: Future[String] = Future {
    val writer = new StringWriter()
    TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
    writer.toString
  }

  override val httpDurationSeconds: Histogram = Metrics.httpDurationSeconds
  override val httpTotalRequests: Counter = Metrics.httpTotalRequests
}
