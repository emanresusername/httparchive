package my.will.be.done.httparchive.cli

import java.io.{File, PrintStream, FileOutputStream}
import my.will.be.done.httparchive.model.Header
import java.nio.charset.StandardCharsets.UTF_8

case class Conf(
    file: File = null,
    output: Option[File] = None,
    tsvDelimiter: String = "\t",
    serial: Boolean = true,
    jsonline: Boolean = false,
    command: Command = null,
    urlStringReplacements: Seq[(String, String)] = Nil,
    emptyResponse: Boolean = false,
    actors: Int = 1
) {
  def outputStream: PrintStream = {
    val overwrite = Command.Modify.equals(command)
    output
      .map(new FileOutputStream(_, !overwrite))
      .map(new PrintStream(_, true, UTF_8.name))
      .getOrElse(System.out)
  }

  def modifyUrl(url: String): String = urlStringReplacements.foldLeft(url) {
    case (url, (find, replace)) ⇒
      url.replaceAllLiterally(find, replace)
  }

  def modifyUrlHeaderValue(header: Header): Header =
    header.name.toLowerCase match {
      case "host" | "referer" ⇒
        header.copy(value = modifyUrl(header.value))
      case _ ⇒
        header
    }
}
