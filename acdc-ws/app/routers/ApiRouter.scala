/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package routers

import javax.inject.Inject

import play.api.routing.sird._
import play.api.routing.{Router, SimpleRouter}
import controllers._

class ApiRouter @Inject() (
  dataset: DatasetController,
  instance: DatasetInstanceController,
  lineage: DatasetLineageController
) extends SimpleRouter {

  override def routes: Router.Routes = {

    case POST(p"/dataset") => dataset.create()
    case PUT(p"/dataset/$name") => dataset.update(name)
    case GET(p"/dataset/$name") => dataset.get(name)
    case DELETE(p"/dataset/$name") => dataset.delete(name)

    case POST(p"/instance") => instance.create()
    case GET(p"/instance/$name") => instance.get(name)
    case DELETE(p"/instance/$name") => instance.delete(name)
    case PUT(p"/instance/$instName/dataset/$datasetName") => instance.mapTo(instName, datasetName)
    case DELETE(p"/instance/$instName/dataset/$datasetName") =>
      instance.removeFrom(instName, datasetName)
    case GET(p"/instances/dataset/$dataset") => instance.forDataset(dataset)

    case PUT(p"/lineage/$destName") => lineage.setSources(destName)
    case GET(p"/lineage/$destName") => lineage.getSources(destName)
    case DELETE(p"/lineage/$destName") => lineage.delete(destName)

  }

}
