/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._

import com.salesforce.mce.acdc.ws.BuildInfo

@Singleton
class StatusController @Inject() (
  cc: ControllerComponents
) extends AbstractController(cc) {

  def status: Action[AnyContent] = Action { _ =>
    Ok(Json.obj("status" -> "ok", "version" -> BuildInfo.version))
  }

  def notFound: Action[AnyContent] = Action { _ => NotFound }

}
