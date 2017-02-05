package my.will.be.done.httparchive

import my.will.be.done.httparchive.cli.BuildInfo
import java.io.{File, PrintStream, FileOutputStream}

package object cli {
  case class Conf(
      file: File = new File("."),
      output: Option[File] = None,
      tsvDelimiter: String = "\t",
      serial: Boolean = true,
      jsonline: Boolean = false
  ) {
    def outputStream: PrintStream = {
      output
        .map(new FileOutputStream(_, true))
        .map(new PrintStream(_))
        .getOrElse(System.out)
    }
  }

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

    opt[File]('o', "output")
      .optional()
      .valueName("<file>")
      .action((x, c) => c.copy(output = Option(x)))
      .text("where to output the replay info, defaults to stdout")

    opt[Unit]('s', "scheduled")
      .action((_, c) => c.copy(serial = false))
      .text(
        "if set the requests will start at the same relative time they started in the source httparchive. if not, they will start immediately after the previous one finishes")

    opt[Unit]('j', "jsonline")
      .action((_, c) => c.copy(jsonline = true))
      .text(
        "if set will output in jsonline format. if not will output for tsv")
  }
}
