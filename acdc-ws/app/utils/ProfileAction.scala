package utils

import play.api.mvc._
import services.MetricReporter
import services.Metrics.apiLatencySummary
import utils.ProfileAction.STATIC_PATH_MARKERS

import scala.concurrent.{ExecutionContext, Future}

case class ProfileAction[A](reporter: MetricReporter)(action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    parseRequest(request) match {
      case Some( (staticPath, argument) ) =>
        val timer = apiLatencySummary.labels (staticPath, argument, request.method).startTimer()
        val actionResult = action (request)
        actionResult.onComplete (_ =>
        timer.close ()
        ) (executionContext)
        actionResult
      case _ => action(request)
    }
  }

  private def parseRequest(request: Request[A]): Option[(String, String)] = {
    if ( reporter.checkPathIsDisabled(request.path) )
      None
    else {
      val (staticPath, args) = request.path
        .split("/")
        .foldLeft((List[String](), List[String]())) {
          case ( (Nil, l2), token) => (List(token), l2)
          case ( (h::t, l2), token ) => if (STATIC_PATH_MARKERS.contains(h)) (h::t, token::l2)
            else (token::h::t, l2)
        }
      Some((staticPath.reverse.mkString("/") , args.reverse.mkString("/")))
    }
  }

  override def parser: BodyParser[A] = action.parser
  override def executionContext: ExecutionContext = action.executionContext
}

object ProfileAction  {
  val STATIC_PATH_MARKERS = Seq("instance", "dataset", "lineage", "__metrics", "__status")
}