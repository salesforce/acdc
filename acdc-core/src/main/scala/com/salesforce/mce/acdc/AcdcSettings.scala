/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.mce.acdc

import com.typesafe.config.{Config, ConfigFactory}

class AcdcSettings private (config: Config) {

  def slickDatabaseConf = config.getConfig("jdbc")

}

object AcdcSettings {

  val configPath = "com.salesforce.mce.acdc"

  def withRootConfig(rootConfig: Config): AcdcSettings = new AcdcSettings(
    rootConfig.getConfig(configPath)
  )

  def apply(): AcdcSettings = withRootConfig(ConfigFactory.load())

}
