package utils

import scala.jdk.CollectionConverters.CollectionHasAsScala

import com.typesafe.config.{Config, ConfigFactory, ConfigList}

class AuthorizationSettings private (config: Config) {

  def authHeader: String = config.getString(s"header-name")

  def authEnabled: Boolean = config.getBoolean(s"enabled")

  def keyRoles: Map[String, List[String]] = config
    .entrySet()
    .asScala
    .filter(_.getKey.startsWith(s"hashed-keys."))
    .map(kv => {
      val key: String = kv.getKey.stripPrefix(s"hashed-keys.")
      val configList: ConfigList = kv.getValue.asInstanceOf[ConfigList]
      val listValue = configList.unwrapped().asScala.map(_.toString).toList
      (key, listValue)
    })
    .toMap

}

object AuthorizationSettings {

  val configPath = "acdc.auth"

  def withRootConfig(rootConfig: Config): AuthorizationSettings = new AuthorizationSettings(
    rootConfig.getConfig(configPath)
  )

  def apply(): AuthorizationSettings = withRootConfig(ConfigFactory.load())

}
