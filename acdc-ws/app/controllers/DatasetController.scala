/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package controllers

import javax.inject._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import com.salesforce.mce.acdc.db.DatasetQuery
import models.CreateDatasetRequest
import models.DatasetResponse
import services.DatabaseService

@Singleton
class DatasetController @Inject() (cc: ControllerComponents, dbService: DatabaseService)(implicit
  ec: ExecutionContext
) extends AcdcAbstractController(cc, dbService) {

  def create() = Action.async(parse.json) {
    _.body
      .validate[CreateDatasetRequest]
      .fold(
        e => Future.successful(BadRequest(JsError.toJson(e))),
        r =>
          db.async(DatasetQuery.ForName(r.name).create()).map {
            case 0 => Conflict
            case 1 => Created
          }
      )
  }

  def update(name: String) = Action.async(parse.json) {
    _.body
      .validate[CreateDatasetRequest]
      .fold(
        e => Future.successful(BadRequest(JsError.toJson(e))),
        r =>
          db.async(DatasetQuery.ForName(name).update(r.name)).map {
            case -1 => Conflict("New name already exists")
            case 0 => NotFound
            case 1 => Ok("Updated")
          }
      )
  }

  def get(name: String) = Action.async {
    db.async(DatasetQuery.ForName(name).get()).map {
      case Some(r) => Ok(Json.toJson(DatasetResponse(r.name, r.createdAt, r.updatedAt)))
      case None => NotFound
    }
  }

  def delete(name: String) = Action.async {
    db.async(DatasetQuery.ForName(name).delete()).map(r => Ok(Json.toJson(r)))
  }

}
