package my.will.be.done.httparchive

import my.will.be.done.httparchive.cli.BuildInfo
import java.io.File

package object cli {
  case class Conf(file: File = new File("."))

  val name: String = {
    Seq(
      BuildInfo.organization.split("\\.").last,
      BuildInfo.name
    ).mkString("-")
  }

  val OptionParser = new scopt.OptionParser[Conf](name) {
    head(name, BuildInfo.version)
    help("help")
    version("version")

    opt[File]('f', "file")
      .required()
      .valueName("<file>")
      .action((x, c) => c.copy(file = x))
      .text("the httparchive (`.har` file) to replay")
  }
}
