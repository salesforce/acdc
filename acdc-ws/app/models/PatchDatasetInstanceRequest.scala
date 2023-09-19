package models

import play.api.libs.json.{Json, Reads, Writes}

case class PatchDatasetInstanceRequest(isActive: Boolean)

object PatchDatasetInstanceRequest {

  implicit val reads: Reads[PatchDatasetInstanceRequest] = Json.reads
  implicit val writes: Writes[PatchDatasetInstanceRequest] = Json.writes

}
