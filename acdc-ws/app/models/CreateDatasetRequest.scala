/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package models

import play.api.libs.json.Json

case class CreateDatasetRequest(name: String)

object CreateDatasetRequest {

  implicit val reads = Json.reads[CreateDatasetRequest]
  implicit val writes = Json.writes[CreateDatasetRequest]

}
