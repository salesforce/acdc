/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package models

import java.time.LocalDateTime

import play.api.libs.json.{Json, Reads, Writes}

case class DatasetResponse(
  name: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime,
  meta: Option[String]
)

object DatasetResponse {

  implicit val reads: Reads[DatasetResponse] = Json.reads
  implicit val writes: Writes[DatasetResponse] = Json.writes

}
