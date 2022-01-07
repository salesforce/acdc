/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package controllers

import services.DatabaseService
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

abstract class AcdcAbstractController(cc: ControllerComponents, dbService: DatabaseService)
    extends AbstractController(cc) {

  def db = dbService.db

}
