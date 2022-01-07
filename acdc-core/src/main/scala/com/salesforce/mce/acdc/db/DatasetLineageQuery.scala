/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc.db

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext

object DatasetLineageQuery {

  case class ForDestination(dest: String) {

    def table = DatasetLineageTable().filter(_.toDataset === dest)

    def delete() = table.delete

    def setSources(sources: Seq[String])(implicit ec: ExecutionContext) = (
      for {
        _ <- delete()
        r <- DatasetLineageTable() ++= sources.map(s => DatasetLineageTable.R(s, dest))
      } yield r
    ).transactionally

    def getSources() = table.map(_.fromDataset).result

  }

}
