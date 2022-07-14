package utils

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import akka.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}
import services.MetricReporter

class MetricFilter @Inject()
(
  metricReporter: MetricReporter
)(implicit
  val mat: Materializer,
  ec: ExecutionContext
) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    if ( metricReporter.checkPathIsDisabled(requestHeader.path) ) {
      nextFilter(requestHeader)
    } else {
      val requestTimer = metricReporter.httpDurationSeconds.startTimer()
      nextFilter(requestHeader).transform(
        result => {
          metricReporter.httpTotalRequests.labels(s"${result.header.status}").inc()

          if (result.header.status < 500 || result.header.status >= 600) {
            requestTimer.close()
          }

          result
        },
        exception => {
          metricReporter.httpTotalRequests.labels("500").inc()

          exception
        }
      )
    }
  }
}