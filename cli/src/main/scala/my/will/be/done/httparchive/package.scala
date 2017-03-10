package my.will.be.done.httparchive

package object cli {
  val cliName: String = {
    Seq(
      BuildInfo.organization.split("\\.").last,
      BuildInfo.name
    ).mkString("-")
  }
}
