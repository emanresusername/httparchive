package my.will.be.done.httparchive

import my.will.be.done.httparchive.cli.BuildInfo
import java.io.File
import my.will.be.done.httparchive.cli.Command._

package object cli {
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

    cmd(Replay.entryName)
      .action((x, c) => c.copy(command = Replay))
      .text("replay the requests in the http archive")
      .children(
        opt[Unit]('s', "scheduled")
          .action((_, c) => c.copy(serial = false))
          .text(
            "if set the requests will start at the same relative time they started in the source httparchive. if not, they will start immediately after the previous one finishes"),
        opt[Unit]('j', "jsonline")
          .action((_, c) => c.copy(jsonline = true))
          .text(
            "if set will output in jsonline format. if not will output for tsv")
      )

    cmd(Modify.entryName)
      .action((x, c) ⇒ c.copy(command = Modify))
      .text("produce a new httpArchive with certain aspects modified")
      .children(
        opt[(String, String)]('u', "url")
          .unbounded()
          .keyName("find")
          .valueName("replace")
          .action((x, c) ⇒
            c.copy(urlStringReplacements = c.urlStringReplacements :+ x))
          .text("does a literal string replacement on the request urls changing all instances of `find` to `replace`. Will also change `Host` and `Referer` headers"),
        opt[Unit]('e', "empty-response")
          .action((_, c) ⇒ c.copy(emptyResponse = true))
          .text("empties the response body text, headers and cookies")
      )
  }
}
