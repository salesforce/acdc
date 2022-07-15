package controllers

import play.api.mvc._
import services.MetricReporter
import services.Metrics.{apiLatencyGauge, apiLatencySummary, clearMetrics, setSumAvgToGauge}

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class MetricController @Inject() (
  cc: ControllerComponents,
  reporter: MetricReporter
 ) (implicit
    ec: ExecutionContext
 )  extends AbstractController(cc) {

  def collect: Action[AnyContent] = Action.async { r: Request[AnyContent] =>
      setSumAvgToGauge(apiLatencySummary, apiLatencyGauge)
      val metricResults = reporter.metrics.map(output => Ok(output))
      metricResults.onComplete(_ => clearMetrics())
      metricResults
    }

 }
