/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package controllers

import javax.inject._

import scala.concurrent.Future

import play.api.libs.json._
import play.api.mvc._

import com.salesforce.mce.acdc.ws.BuildInfo
import utils.{AuthTransformAction, InvalidApiRequest, ValidApiRequest}

@Singleton
class StatusController @Inject() (cc: ControllerComponents, authAction: AuthTransformAction)
    extends AbstractController(cc) {

  def status() = authAction.async { request =>
    request match {
      case ValidApiRequest(apiRole, req) =>
        Future.successful(Ok(Json.obj("status" -> "ok", "version" -> BuildInfo.version)))
      case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
    }
  }

}
