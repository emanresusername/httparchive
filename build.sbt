lazy val commonSettings = Seq(
  scalaVersion := "2.12.1",
  organization := "my.will.be.done.httparchive",
  version := "0.0.1",
  scalacOptions ++= Seq("-deprecation", "-feature")
)

val Version = new {
  val rapture  = "2.0.0-M8"
  val akka     = "2.4.16"
  val akkaHttp = "10.0.3"
}

def jvmDeps(org: String, version: String)(names: String*) = {
  for {
    name ← names
  } yield {
    org %% name % version
  }
}

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .settings(commonSettings: _*)

lazy val coreJvm = core.jvm

lazy val rapture = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++=
      jvmDeps("com.propensive", Version.rapture)(
        "rapture-json-circe",
        "rapture-io",
        "rapture-uri",
        "rapture-net"
      )
  )
  .dependsOn(coreJvm)

val akkaOrg = "com.typesafe.akka"
val akkaOrgNames = Seq(
  "akka-actor"
).map(akkaOrg → _)

lazy val akka = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++=
      jvmDeps(akkaOrg, Version.akka)(
        "akka-actor"
      ) ++
        jvmDeps(akkaOrg, Version.akkaHttp)(
          "akka-http-core"
        )
  )
  .dependsOn(coreJvm)

lazy val cli = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= {

      Seq(
        "com.github.scopt" %% "scopt" % "3.5.0"
      )
    },
    buildInfoKeys := Seq[BuildInfoKey](name, version, organization),
    buildInfoPackage := s"${organization.value}.${name.value}"
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(rapture)
