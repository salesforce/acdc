package utils

import scala.jdk.CollectionConverters.CollectionHasAsScala

import com.typesafe.config.{Config, ConfigFactory}

class AuthorizationSettings private (config: Config) {

  def authHeader: String = config.getString(s"header-name")

  def authEnabled: Boolean = config.getBoolean(s"enabled")

  def keyRoles: Map[String, List[String]] = config
    .entrySet()
    .asScala
    .filter(_.getKey.startsWith(s"hashed-keys"))
    .flatMap(kv => {
      val stripQuote = kv.getValue.toString.stripPrefix("Quoted(\"").stripSuffix("\")")
      getHashedKeysMap(stripQuote)
    })
    .toMap

  /**
   * regex parse input string to a map of x-api-key, value being a list of associated roles
   *
   * @param s  input string expected of this format
   *           "{ f6e42a3c0dffee079face0da061ee2c9a871eebe098ac481248e34cfe023955b = [admin, user]
   *           , 70ad8c1543728a3e61bbec26d1df5bd742d1c2f464f2ac54a2fec5e709eba890 = [admin] }"
   * @return map of x-api-key, value being a list of associated roles
   */
  def getHashedKeysMap(s: String): Map[String, List[String]] = {
    // regex: { hashed_key1 = [role1, role2], key2 = [role3, role4] }
    lazy val keyVal = raw"[,\s{]*(\w+)\s*=\s*\[([\w\W]*?)]+?[\s}]*".r

    // regex for role1, role2
    lazy val valList = raw"([\s,]*)([^\s,]+)\s*".r

    val s2 = s.replaceAll("""\\""" + """\"""", "").replaceAll("""\"""", "")
    println(s"AuthorizationSettings.getHashedKeysMap: s=$s")
    println(s"AuthorizationSettings.getHashedKeysMap: s2=$s2")
    keyVal
      .findAllIn(s2)
      .map {
        // key would be a hashed key of a x-api-key; vList would be roles (example: admin, user)
        case keyVal(key, vList) =>
          (
            key,
            valList
              .findAllIn(vList)
              .map {
                case valList(_, v) => v // ignore the comma separator
                case _ => None.asInstanceOf[String]
              }
              .toList
          )
        case _ => ().asInstanceOf[Tuple2[String, List[String]]]
      }
      .toMap
  }

}

object AuthorizationSettings {

  val configPath = "acdc.auth"

  def withRootConfig(rootConfig: Config): AuthorizationSettings = new AuthorizationSettings(
    rootConfig.getConfig(configPath)
  )

  def apply(): AuthorizationSettings = withRootConfig(ConfigFactory.load())

}
