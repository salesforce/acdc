val slickVersion = "3.3.3"

val scalaTestArtifact = "org.scalatest" %% "scalatest" % "3.2.+" % Test
val prometheusClient = "io.prometheus" % "simpleclient" % "0.16.0"
val prometheusCommon = "io.prometheus" % "simpleclient_common" % "0.16.0"
val prometheusHotSpot = "io.prometheus" % "simpleclient_hotspot" % "0.16.0"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint"), // , "-Xfatal-warnings"),
  scalaVersion := "2.13.8",
  libraryDependencies += scalaTestArtifact,
  fork := true,
  organization := "com.salesforce.mce",
  assembly / test := {},  // skip test during assembly
  headerLicense := Some(HeaderLicense.Custom(
  """|Copyright (c) 2021, salesforce.com, inc.
     |All rights reserved.
     |SPDX-License-Identifier: BSD-3-Clause
     |For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
     |""".stripMargin
  ))
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "acdc"
  ).
  aggregate(core, ws)

lazy val core = (project in file("acdc-core")).
  settings(commonSettings: _*).
  settings(
    name := "acdc-core",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "org.postgresql" % "postgresql" % "42.4.+"
    )
  )

lazy val ws = (project in file("acdc-ws")).
  enablePlugins(PlayScala, BuildInfoPlugin).
  settings(commonSettings: _*).
  settings(
    name := "acdc-ws",
    buildInfoKeys := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "com.salesforce.mce.acdc.ws",
    libraryDependencies ++= Seq(
      guice,
      prometheusClient,
      prometheusCommon,
      prometheusHotSpot
    ),
    dependencyOverrides ++= Seq(
      // fix https://nvd.nist.gov/vuln/detail/CVE-2020-36518
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.4",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.4"
    )
  ).
  dependsOn(core)
