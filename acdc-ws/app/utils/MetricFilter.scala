package utils

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import akka.stream.Materializer

import play.api.mvc.{Filter, RequestHeader, Result}

import services.Metric

class MetricFilter @Inject() (
  metric: Metric
)(implicit
  val mat: Materializer,
  ec: ExecutionContext
) extends Filter {

  private val patt = raw"(api-v1-[a-z]+).*".r // keep only first word after api-v1
  def apply(
    nextFilter: RequestHeader => Future[Result]
  )(requestHeader: RequestHeader): Future[Result] = {
    metric.parseRequest(requestHeader) match {
      case Some((staticPath, _)) =>
        // simplify path to control prometheus summary metric label cardinality
        val simplePath = staticPath match {
          case patt(prefix) => prefix
          case _ => ""
        }
        // hardcode argument to "" so as to control the number of time series
        val stopTimerCallback = metric.startApiTimer(simplePath, requestHeader.method)

        nextFilter(requestHeader)
          .transform(
            result => {
              metric.incrementStatusCount(result.header.status.toString)
              stopTimerCallback()
              result
            },
            exception => {
              metric.incrementStatusCount("500")
              stopTimerCallback()
              exception
            }
          )
      case _ =>
        nextFilter(requestHeader)
    }
  }
}
