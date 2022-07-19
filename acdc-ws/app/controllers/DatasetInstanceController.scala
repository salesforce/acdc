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
import play.api.libs.json.{JsError, JsNull, JsString, JsValue, Json}
import play.api.mvc._
import com.salesforce.mce.acdc.db.DatasetInstanceQuery
import com.salesforce.mce.acdc.db.DatasetInstanceTable
import models.{DatasetInstanceResponse, PatchDatasetInstanceRequest, PostDatasetInstanceRequest}
import services.{DatabaseService}
import utils.{AuthTransformAction, InvalidApiRequest, ValidApiRequest}

@Singleton
class DatasetInstanceController @Inject() (
  cc: ControllerComponents,
  dbService: DatabaseService,
  authAction: AuthTransformAction
)(implicit
  ec: ExecutionContext
) extends AcdcAbstractController(cc, dbService) {

  private def toResponse(r: DatasetInstanceTable.R): JsValue = Json.toJson(
    DatasetInstanceResponse(
      r.dataset,
      r.name,
      r.createdAt,
      r.updatedAt,
      r.location,
      r.isActive,
      r.meta
    )
  )

  def create() = authAction.async(parse.json) {
    case ValidApiRequest(apiRole, req) =>
      req.body
        .validate[PostDatasetInstanceRequest]
        .fold(
          e => Future.successful(BadRequest(JsError.toJson(e))),
          r =>
            db.async(DatasetInstanceQuery.ForInstance(r.dataset, r.name).create(r.location, r.meta))
              .map {
                case Left(r) => Conflict(toResponse(r))
                case Right(r) => Created(toResponse(r))
              }
        )
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def get(dataset: String, name: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      db.async(DatasetInstanceQuery.ForInstance(dataset, name).get()).map {
        case Some(r) => Ok(toResponse(r))
        case None => NotFound(JsNull)
      }
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def delete(dataset: String, name: String) = authAction.async {
    case ValidApiRequest(apiRole, req) =>
      db.async(DatasetInstanceQuery.ForInstance(dataset, name).delete())
        .map(r => Ok(Json.toJson(r)))
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def patch(dataset: String, name: String) = authAction.async(parse.json) {
    case ValidApiRequest(apiRole, req) =>
      req.body
        .validate[PatchDatasetInstanceRequest]
        .fold(
          e => Future.successful(BadRequest(JsError.toJson(e))),
          r =>
            db.async(
              DatasetInstanceQuery
                .ForInstance(dataset, name)
                .setActivation(r.isActive)
                .map(r => Ok(Json.toJson(r)))
            )
        )
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def forDataset(dataset: String) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      for {
        rs <- db.async(DatasetInstanceQuery.forDataset(dataset))
      } yield Ok(Json.toJson(rs.map(toResponse)))
    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

  def filter(
    dataset: Option[String],
    like: Option[String],
    orderBy: Option[String],
    order: Option[String],
    page: Option[Int],
    perPage: Option[Int]
  ) = authAction.async {
    case ValidApiRequest(apiRole, _) =>
      val validated = for {
        vOrderBy <- DatasetInstanceController.validateOrderBy(orderBy)
        vOrder <- DatasetInstanceController.validateOrder(order)
        vPage <- DatasetInstanceController.validateOne(page.getOrElse(1), "page")
        limit <- DatasetInstanceController.validateOne(perPage.getOrElse(50), "per_page")
      } yield (vOrderBy, vOrder, limit, (vPage - 1) * limit)

      validated match {
        case Right((by, ord, limit, offset)) =>
          db.async(DatasetInstanceQuery.filter(dataset, like, by, ord, limit, offset))
            .map(rs => Ok(Json.toJson(rs.map(toResponse))))
        case Left(msg) =>
          Future.successful(BadRequest(JsString(msg)))
      }

    case InvalidApiRequest(_) => Future.successful(Unauthorized(JsNull))
  }

}

object DatasetInstanceController {

  private def validateOrderBy(
    orderBy: Option[String]
  ): Either[String, DatasetInstanceQuery.OrderBy] = {
    orderBy
      .fold[Either[String, DatasetInstanceQuery.OrderBy]](
        Right(DatasetInstanceQuery.OrderBy.CreatedAt)
      ) {
        case "created_at" => Right(DatasetInstanceQuery.OrderBy.CreatedAt)
        case "updated_at" => Right(DatasetInstanceQuery.OrderBy.UpdatedAt)
        case other => Left(s"Unknow order_by $other")
      }
  }

  private def validateOrder(
    order: Option[String]
  ): Either[String, DatasetInstanceQuery.Order] = {
    order
      .fold[Either[String, DatasetInstanceQuery.Order]](
        Right(DatasetInstanceQuery.Order.Desc)
      ) {
        case "desc" => Right(DatasetInstanceQuery.Order.Desc)
        case "asc" => Right(DatasetInstanceQuery.Order.Asc)
        case other => Left(s"Unknow order $other")
      }
  }

  private def validateOne(num: Int, name: String): Either[String, Int] = {
    if (num < 1) Left(s"$name must be at least 1")
    else Right(num)
  }

}
