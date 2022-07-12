package utils

import play.api.mvc._
import utils.Metrics.apiLatencySummary
import utils.ProfileAction.API_METHODS

import scala.concurrent.{ExecutionContext, Future}

case class ProfileAction[A](reporter: MetricReporter)(action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    parseRequest(request) match {
      case Some( (staticPath, argument) ) =>
        val timer = apiLatencySummary.labels (staticPath, argument, request.method).startTimer ()
        val actionResult = action (request)
        actionResult.onComplete (_ =>
        timer.close ()
        ) (executionContext)
        actionResult
      case _ => action(request)
    }
  }

  private def parseRequest(request: Request[A]): Option[(String, String)] = {
    val pathTokens = request.path.split("/")
    API_METHODS.map{ api => pathTokens
      .indexOf(api)}
      .find(_ != -1)
      .map{ i =>
        val staticPath = pathTokens.slice(0, i+1).mkString("/")
        val args = pathTokens.slice(i+1, pathTokens.length).mkString("/")
        (staticPath, args)
      }
  }

  override def parser: BodyParser[A] = action.parser
  override def executionContext: ExecutionContext = action.executionContext
}

object ProfileAction  {
  val API_METHODS = Seq("instance", "dataset", "lineage", "__metrics", "__status")
}