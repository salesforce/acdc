package models

import play.api.libs.json.Json

case class PatchDatasetInstanceRequest(isActive: Boolean)

object PatchDatasetInstanceRequest {

  implicit val reads = Json.reads[PatchDatasetInstanceRequest]
  implicit val writes = Json.writes[PatchDatasetInstanceRequest]

}
