val slickVersion = "3.4.1"

val scalaTestArtifact = "org.scalatest" %% "scalatest" % "3.2.+" % Test
val kineticpulse = "com.salesforce.mce" %% "kineticpulse-metric" % "0.2.+"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint"), // , "-Xfatal-warnings"),
  scalaVersion := "2.13.10",
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
      "org.postgresql" % "postgresql" % "42.6.0"
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
      kineticpulse
    ),
    dependencyOverrides ++= Seq(
      // the transitive jackson dependencies from play framework on has security vulnerabilities
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.1",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.1"
    )
  ).
  dependsOn(core)
