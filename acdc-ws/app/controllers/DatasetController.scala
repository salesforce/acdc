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
import com.salesforce.mce.acdc.db.{DatasetQuery, DatasetTable}
import models.CreateDatasetRequest
import models.DatasetResponse
import play.api.libs.json.JsString
import services.{DatabaseService}
import utils.{AuthTransformAction, InvalidApiRequest, ValidApiRequest}

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

  def filter(
    like: String,
    orderBy: Option[String],
    order: Option[String],
    page: Option[Int],
    perPage: Option[Int]
  ) = authAction.async {
    case ValidApiRequest(apiRole, _) =>

      val validated = for {
        vOrderBy <- DatasetController.validateOrderBy(orderBy)
        vOrder <- DatasetController.validateOrder(order)
        vPage <- DatasetController.validateOne(page.getOrElse(1), "page")
        limit <- DatasetController.validateOne(perPage.getOrElse(50), "per_page")
      } yield (vOrderBy, vOrder, limit, (vPage - 1) * limit)

      validated match {
        case Right((by, ord, limit, offset)) =>
          db.async(DatasetQuery.filter(like, by, ord, limit, offset))
            .map(rs => Ok(Json.toJson(rs.map(toResponse))))
        case Left(msg) =>
          Future.successful(BadRequest(JsString(msg)))
      }

    case InvalidApiRequest(_) =>
      Future.successful(Unauthorized(JsNull))
  }

}

object DatasetController {

  private def validateOrderBy(
    orderBy: Option[String]
  ): Either[String, DatasetQuery.OrderBy] = {
    orderBy
      .fold[Either[String, DatasetQuery.OrderBy]](
        Right(DatasetQuery.OrderBy.CreatedAt)
      ) {
        case "created_at" => Right(DatasetQuery.OrderBy.CreatedAt)
        case "updated_at" => Right(DatasetQuery.OrderBy.UpdatedAt)
        case other => Left(s"Unknow order_by $other")
      }
  }

  private def validateOrder(
    order: Option[String]
  ): Either[String, DatasetQuery.Order] = {
    order
      .fold[Either[String, DatasetQuery.Order]](
        Right(DatasetQuery.Order.Desc)
      ) {
        case "desc" => Right(DatasetQuery.Order.Desc)
        case "asc" => Right(DatasetQuery.Order.Asc)
        case other => Left(s"Unknow order $other")
      }
  }


  private def validateOne(num: Int, name: String): Either[String, Int] = {
    if (num < 1) Left(s"$name must be at least 1")
    else Right(num)
  }

}
