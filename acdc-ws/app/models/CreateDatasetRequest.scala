/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package models

import play.api.libs.json.{Json, Reads, Writes}

case class CreateDatasetRequest(name: String, meta: Option[String])

object CreateDatasetRequest {

  implicit val reads: Reads[CreateDatasetRequest] = Json.reads
  implicit val writes: Writes[CreateDatasetRequest] = Json.writes

}
