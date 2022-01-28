package utils

import play.api.mvc.{Request, WrappedRequest}

sealed trait ApiRequest[A]

case class ValidApiRequest[A](apiRole: String, request: Request[A])
    extends WrappedRequest[A](request) with ApiRequest[A]

case class InvalidApiRequest[A](request: Request[A])
    extends WrappedRequest[A](request) with ApiRequest[A]
