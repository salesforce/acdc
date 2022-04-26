package utils

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging
import play.api.mvc.{ActionBuilder, ActionTransformer, AnyContent, BodyParsers, Request}

class AuthTransformAction @Inject() (val parser: BodyParsers.Default, auth: Authorization)(implicit
  val executionContext: ExecutionContext
) extends ActionBuilder[ApiRequest, AnyContent] with ActionTransformer[Request, ApiRequest]
    with Logging {

  override def transform[A](request: Request[A]) =
    Future.successful {
      auth.getRoles(request) match {
        case Nil =>
          logger.debug(s"transform not found x-api-key")
          InvalidApiRequest(request)
        case xs =>
          logger.debug(s"transform found x-api-key $xs")
          if (xs.contains(Authorization.Admin)) {
            ValidApiRequest(Authorization.Admin, request)
          } else if (xs.contains(Authorization.User)) {
            ValidApiRequest(Authorization.User, request)
          } else {
            InvalidApiRequest(request)
          }
      }
    }

}
