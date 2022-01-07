/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package services

import javax.inject._

import play.api.Configuration

import com.salesforce.mce.acdc.db.AcdcDatabase
import com.salesforce.mce.acdc.AcdcSettings

@Singleton
class DatabaseService @Inject() (conf: Configuration) {

  val db = AcdcDatabase(AcdcSettings.withRootConfig(conf.underlying))

}
