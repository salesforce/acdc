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

import com.salesforce.mce.acdc.db.DatasetInstanceQuery
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

  def create() = authAction.async(parse.json) {
    case ValidApiRequest(apiRole, req) =>
      req.body
        .validate[PostDatasetInstanceRequest]
        .fold(
          e => Future.successful(BadRequest(JsError.toJson(e))),
          r =>
            db.async(DatasetInstanceQuery.ForName(r.name).create(r.location)).map {
              case 0 => Conflict
              case 1 => Created
            }
        )
    case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
  }

  def get(name: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetInstanceQuery.ForName(name).get()).map {
        case Some(r) =>
          Ok(Json.toJson(DatasetInstanceResponse(r.name, r.location, r.dataset, r.createdAt)))
        case None => NotFound
      }
    case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
  }

  def delete(name: String) = authAction.async {
    case ValidApiRequest(apiRole, req) =>
      db.async(DatasetInstanceQuery.ForName(name).delete()).map(r => Ok(Json.toJson(r)))
    case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
  }

  def mapTo(instName: String, datasetName: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetInstanceQuery.ForName(instName).mapTo(datasetName))
        .map(r => Ok(Json.toJson(r)))
    case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
  }

  def removeFrom(instName: String, datasetName: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetInstanceQuery.ForName(instName).removeFrom(datasetName))
        .map(r => Ok(Json.toJson(r)))
    case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
  }

}
