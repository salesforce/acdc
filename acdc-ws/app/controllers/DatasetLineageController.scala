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

import com.salesforce.mce.acdc.db.DatasetLineageQuery
import services.DatabaseService

@Singleton
class DatasetLineageController @Inject() (cc: ControllerComponents, dbService: DatabaseService)(
  implicit ec: ExecutionContext
) extends AcdcAbstractController(cc, dbService) {

  def setSources(dest: String) = Action.async(parse.json) {
    _.body
      .validate[Seq[String]]
      .fold(
        e => Future.successful(BadRequest(JsError.toJson(e))),
        rs =>
          db.async(DatasetLineageQuery.ForDestination(dest).setSources(rs))
            .map(r => Created(Json.toJson(r.getOrElse(0))))
      )
  }

  def getSources(dest: String) = Action.async {
    db.async(DatasetLineageQuery.ForDestination(dest).getSources()).map(rs => Ok(Json.toJson(rs)))
  }

  def delete(dest: String) = Action.async {
    db.async(DatasetLineageQuery.ForDestination(dest).delete()).map(r => Ok(Json.toJson(r)))
  }

}
