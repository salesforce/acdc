addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.10")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")

// https://eed3si9n.com/sbt-1.8.0
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
