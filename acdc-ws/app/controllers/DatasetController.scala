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

import play.api.libs.json.{JsError, JsNull, JsValue, Json}
import play.api.mvc._

import com.salesforce.mce.acdc.db.DatasetQuery
import com.salesforce.mce.acdc.db.DatasetTable
import models.CreateDatasetRequest
import models.DatasetResponse
import services.DatabaseService
import utils.{AuthTransformAction, InvalidApiRequest, ValidApiRequest}
import play.api.libs.json.JsString

@Singleton
class DatasetController @Inject() (
  cc: ControllerComponents,
  dbService: DatabaseService,
  authAction: AuthTransformAction
)(implicit
  ec: ExecutionContext
) extends AcdcAbstractController(cc, dbService) {

  private def toResponse(r: DatasetTable.R): JsValue =
    Json.toJson(DatasetResponse(r.name, r.createdAt, r.updatedAt, r.meta))

  def create() = authAction.async(parse.json) {
    case ValidApiRequest(apiRole, req) =>
      req.body
        .validate[CreateDatasetRequest]
        .fold(
          e => Future.successful(BadRequest(JsError.toJson(e))),
          r =>
            db.async(DatasetQuery.ForName(r.name).create(r.meta)).map {
              case Left(r) => Conflict(toResponse(r))
              case Right(r) => Created(toResponse(r))
            }
        )
    case InvalidApiRequest(_) =>
      Future.successful(Unauthorized(JsNull))
  }

  def update(name: String) = authAction.async(parse.json) {
    case ValidApiRequest(apiRole, req) =>
      req.body
        .validate[CreateDatasetRequest]
        .fold(
          e => Future.successful(BadRequest(JsError.toJson(e))),
          r =>
            db.async(DatasetQuery.ForName(name).update(r.name)).flatMap {
              case Left(r) =>
                Future.successful(Conflict(toResponse(r)))
              case Right(0) =>
                Future.successful(NotFound(JsNull))
              case Right(_) =>
                db.async(DatasetQuery.ForName(r.name).get())
                  .map(newR => Ok(Json.toJson(newR.map(toResponse))))
            }
        )
    case InvalidApiRequest(_) =>
      Future.successful(Unauthorized(JsNull))
  }

  def get(name: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetQuery.ForName(name).get()).map {
        case Some(r) => Ok(toResponse(r))
        case None => NotFound(JsNull)
      }
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def delete(name: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetQuery.ForName(name).delete()).map {
        case -1 => BadRequest(JsString("Cannot delete dataset with instances"))
        case r => Ok(Json.toJson(r))
      }
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def filter(like: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetQuery.filter(like)).map(rs => Ok(Json.toJson(rs.map(toResponse))))
    case InvalidApiRequest(_) =>
      Future.successful(Unauthorized(JsNull))
  }

}
