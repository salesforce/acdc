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

@Singleton
class DatasetInstanceController @Inject() (cc: ControllerComponents, dbService: DatabaseService)(
  implicit ec: ExecutionContext
) extends AcdcAbstractController(cc, dbService) {

  def create() = Action.async(parse.json) {
    _.body
      .validate[PostDatasetInstanceRequest]
      .fold(
        e => Future.successful(BadRequest(JsError.toJson(e))),
        r =>
          db.async(DatasetInstanceQuery.ForName(r.name).create(r.location)).map {
            case 0 => Conflict
            case 1 => Created
          }
      )
  }

  def get(name: String) = Action.async {
    db.async(DatasetInstanceQuery.ForName(name).get()).map {
      case Some(r) => Ok(Json.toJson(DatasetInstanceResponse(r.name, r.location, r.createdAt)))
      case None => NotFound
    }
  }

  def delete(name: String) = Action.async {
    db.async(DatasetInstanceQuery.ForName(name).delete()).map(r => Ok(Json.toJson(r)))
  }

  def mapTo(instName: String, datasetName: String) = Action.async {
    db.async(DatasetInstanceQuery.ForName(instName).mapTo(datasetName)).map(r => Ok(Json.toJson(r)))
  }

  def removeFrom(instName: String, datasetName: String) = Action.async {
    db.async(DatasetInstanceQuery.ForName(instName).removeFrom(datasetName)).map(r => Ok(Json.toJson(r)))
  }

}
