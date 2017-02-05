lazy val commonSettings = Seq(
  scalaVersion := "2.12.1",
  organization := "my.will.be.done.httparchive",
  version := "0.0.1",
  scalacOptions ++= Seq("-deprecation", "-feature")
)

val raptureVersion = "2.0.0-M8"
val raptureOrgNames = Seq(
  "rapture-json-circe",
  "rapture-io",
  "rapture-uri",
  "rapture-net"
).map("com.propensive" → _)

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .settings(commonSettings: _*)

lazy val coreJvm = core.jvm

lazy val rapture = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= {
      for {
        (org, name) ← raptureOrgNames
      } yield {
        org %% name % raptureVersion
      }
    }
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
