/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package utils

import java.net.URL

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success, Try}

import com.typesafe.config.ConfigException
import com.typesafe.config.{Config, ConfigFactory, ConfigList}

class AuthorizationSettings private (config: Config) {

  def authHeader: String = config.getString(s"header-name")

  def authEnabled: Boolean = config.getBoolean(s"enabled")

  def keyRoles: Map[String, List[String]] = config
    .entrySet()
    .asScala
    .filter(_.getKey.startsWith(s"hashed-keys."))
    .foldLeft(Map[String, List[String]]())((b, kv) => {
      val k: String = kv.getKey.stripPrefix(s"hashed-keys.")
      val configList: ConfigList = kv.getValue.asInstanceOf[ConfigList]
      val v = configList.unwrapped().asScala.map(_.toString).toList
      (b -- v) ++ v.map(j => (j, b.getOrElse(j, List.empty) ++ List(k))).toMap
    })

  def ttl: Option[Long] = Try(config.getInt(s"ttl")) match {
    case Success(d) => Some(d)
    case Failure(e: ConfigException.Missing) => None
    case Failure(e) => throw e
  }

}

object AuthorizationSettings {

  val configPath = "acdc.auth"

  def withRootConfig(rootConfig: Config): AuthorizationSettings = new AuthorizationSettings(
    rootConfig.getConfig(configPath)
  )

  private def loadConfig(): Config = Option(System.getProperty("acdc.auth.config"))
    .orElse(Option(System.getenv("ACDC_AUTH_CONFIG_URL")))
    .map(url => ConfigFactory.load(ConfigFactory.parseURL(new URL(url))))
    .getOrElse(ConfigFactory.load())

  def apply(): AuthorizationSettings = withRootConfig(loadConfig())

}
