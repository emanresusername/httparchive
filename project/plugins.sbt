Seq(
  "org.scala-js" % "sbt-scalajs"   % "0.6.14",
  "com.eed3si9n" % "sbt-buildinfo" % "0.6.1",
  "com.lihaoyi"  % "workbench"     % "0.3.0",
  "com.geirsson" % "sbt-scalafmt"  % "0.5.8"
).map(addSbtPlugin)
