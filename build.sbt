lazy val commonSettings = Seq(
  scalaVersion := "2.12.1",
  organization := "my.will.be.done.httparchive",
  version := "0.0.1",
  scalacOptions ++= Seq("-deprecation", "-feature")
)

val Version = new {
  val rapture  = "2.0.0-M8"
  val akka     = "2.4.17"
  val akkaHttp = "10.0.3"
  val circe    = "0.7.0"
  val binding  = "10.0.2"
}

def commonDeps(org: String, version: String)(
    names: String*): Seq[(String, String, String)] = {
  for {
    name ← names
  } yield {
    (org, name, version)
  }
}

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .settings(commonSettings: _*)

lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

val circeCommonDeps = commonDeps("io.circe", Version.circe)(
  "circe-parser",
  "circe-generic",
  "circe-optics"
)

lazy val circe = crossProject
  .crossType(CrossType.Pure)
  .settings(commonSettings: _*)
  .jvmSettings(
    libraryDependencies ++= (
      circeCommonDeps
    ).map {
      case (a, b, c) ⇒ a %% b % c
    }
  )
  .jsSettings(
    libraryDependencies ++= (
      circeCommonDeps
    ).map {
      case (a, b, c) ⇒ a %%% b % c
    }
  )
  .dependsOn(core)
  .enablePlugins(ScalaJSPlugin)

lazy val circeJvm = circe.jvm
lazy val circeJs  = circe.js

val raptureCommonDeps = commonDeps("com.propensive", Version.rapture)(
  "rapture-json-circe",
  "rapture-io",
  "rapture-uri",
  "rapture-net"
)

lazy val rapture = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= (
      raptureCommonDeps
    ).map {
      case (a, b, c) ⇒ a %% b % c
    }
  )
  .dependsOn(circeJvm)

lazy val www = project
  .settings(commonSettings: _*)
  .settings(
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= {
      Seq(
        "com.thoughtworks.binding" %%% "dom" % Version.binding
      )
    }
  )
  .dependsOn(circeJs)
  .enablePlugins(WorkbenchPlugin)

val akkaOrg = "com.typesafe.akka"
val akkaOrgNames = Seq(
  "akka-actor"
).map(akkaOrg → _)

lazy val akka = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= (
      commonDeps(akkaOrg, Version.akka)(
        "akka-actor",
        "akka-agent"
      ) ++
        commonDeps(akkaOrg, Version.akkaHttp)(
          "akka-http-core"
        )
    ).map {
      case (a, b, c) ⇒ a %% b % c
    }
  )
  .dependsOn(coreJvm)

lazy val cli = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= {

      Seq(
        "com.github.scopt" %% "scopt"      % "3.5.0",
        "com.beachape"     %% "enumeratum" % "1.5.7"
      )
    },
    buildInfoKeys := Seq[BuildInfoKey](name, version, organization),
    buildInfoPackage := s"${organization.value}.${name.value}"
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(rapture, akka)
