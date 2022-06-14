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
    case GET(
          p"/datasets" ?
          q"like=$like" &
          q_o"order=$order" &
          q_o"page=${int(page)}" &
          q_o"per_page=${int(perPage)}"
        ) =>
      dataset.filter(like, order, page, perPage)
    case DELETE(p"/dataset/$name") => dataset.delete(name)

    case POST(p"/instance") => instance.create()
    case GET(p"/instance/$dataset/$name") => instance.get(dataset, name)
    case DELETE(p"/instance/$dataset/$name") => instance.delete(dataset, name)
    case PATCH(p"/instance/$dataset/$name") => instance.patch(dataset, name)

    case GET(p"/instances/$dataset") => instance.forDataset(dataset)

    case PUT(p"/lineage/$destName") => lineage.setSources(destName)
    case GET(p"/lineage/$destName") => lineage.getSources(destName)
    case DELETE(p"/lineage/$destName") => lineage.delete(destName)

  }

}
