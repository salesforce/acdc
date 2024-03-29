/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package models

import java.time.LocalDateTime

import play.api.libs.json.{Json, Reads, Writes}

case class DatasetInstanceResponse(
  dataset: String,
  name: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime,
  location: String,
  isActive: Boolean,
  meta: Option[String]
)

object DatasetInstanceResponse {

  implicit val reads: Reads[DatasetInstanceResponse] = Json.reads

  implicit val writes: Writes[DatasetInstanceResponse] = Json.writes
}
