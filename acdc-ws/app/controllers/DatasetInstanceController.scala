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

import com.salesforce.mce.acdc.db.DatasetInstanceQuery
import com.salesforce.mce.acdc.db.DatasetInstanceTable
import models.{DatasetInstanceResponse, PostDatasetInstanceRequest}
import services.DatabaseService
import utils.{AuthTransformAction, InvalidApiRequest, ValidApiRequest}

@Singleton
class DatasetInstanceController @Inject() (
  cc: ControllerComponents,
  dbService: DatabaseService,
  authAction: AuthTransformAction
)(implicit
  ec: ExecutionContext
) extends AcdcAbstractController(cc, dbService) {

  private def toResponse(r: DatasetInstanceTable.R): JsValue =
    Json.toJson(DatasetInstanceResponse(r.name, r.location, r.dataset, r.createdAt))

  def create() = authAction.async(parse.json) {
    case ValidApiRequest(apiRole, req) =>
      req.body
        .validate[PostDatasetInstanceRequest]
        .fold(
          e => Future.successful(BadRequest(JsError.toJson(e))),
          r =>
            db.async(DatasetInstanceQuery.ForName(r.name).create(r.location)).map {
              case Left(r) => Conflict(toResponse(r))
              case Right(r) => Created(toResponse(r))
            }
        )
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def get(name: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetInstanceQuery.ForName(name).get()).map {
        case Some(r) => Ok(toResponse(r))
        case None => NotFound(JsNull)
      }
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def delete(name: String) = authAction.async {
    case ValidApiRequest(apiRole, req) =>
      db.async(DatasetInstanceQuery.ForName(name).delete()).map(r => Ok(Json.toJson(r)))
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def mapTo(instName: String, datasetName: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetInstanceQuery.ForName(instName).mapTo(datasetName))
        .map(r => Ok(Json.toJson(r)))
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def removeFrom(instName: String, datasetName: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetInstanceQuery.ForName(instName).removeFrom(datasetName))
        .map(r => Ok(Json.toJson(r)))
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

}
