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
import utils.{AuthTransformAction, InvalidApiRequest, ValidApiRequest}

@Singleton
class DatasetController @Inject() (
  cc: ControllerComponents,
  dbService: DatabaseService,
  authAction: AuthTransformAction
)(implicit
  ec: ExecutionContext
) extends AcdcAbstractController(cc, dbService) {

  def create() = authAction.async(parse.json) { request =>
    request match {
      case ValidApiRequest(apiRole, req) =>
        req.body
          .validate[CreateDatasetRequest]
          .fold(
            e => Future.successful(BadRequest(JsError.toJson(e))),
            r =>
              db.async(DatasetQuery.ForName(r.name).create()).map {
                case 0 => Conflict
                case 1 => Created
              }
          )
      case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
    }
  }

  def update(name: String) = authAction.async(parse.json) { request =>
    request match {
      case ValidApiRequest(apiRole, req) =>
        req.body
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
      case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
    }
  }

  def get(name: String) = authAction.async { request =>
    request match {
      case ValidApiRequest(apiRole, _) =>
        db.async(DatasetQuery.ForName(name).get()).map {
          case Some(r) => Ok(Json.toJson(DatasetResponse(r.name, r.createdAt, r.updatedAt)))
          case None => NotFound
        }
      case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
    }
  }

  def delete(name: String) = authAction.async { request =>
    request match {
      case ValidApiRequest(apiRole, _) =>
        db.async(DatasetQuery.ForName(name).delete()).map(r => Ok(Json.toJson(r)))
      case InvalidApiRequest(_) => Future.successful(Results.Unauthorized)
    }
  }

}
