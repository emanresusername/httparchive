package my.will.be.done.httparchive.cli

import java.io.{File, PrintStream, FileOutputStream}
import my.will.be.done.httparchive.model.{Entry, Header}
import java.nio.charset.StandardCharsets.UTF_8
import io.circe.Json
import my.will.be.done.httparchive.circe
import scala.util.matching.Regex

case class Conf(
    file: File = null,
    output: Option[File] = None,
    tsvDelimiter: String = "\t",
    serial: Boolean = true,
    jsonline: Boolean = false,
    command: Command = null,
    urlStringReplacements: Seq[(String, String)] = Nil,
    emptyResponse: Boolean = false,
    actors: Int = 1,
    urlFilterRegex: Option[Regex] = None,
    methodFilterRegex: Option[Regex] = None,
    statusMax: Option[Int] = None,
    statusMin: Option[Int] = None
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

  def filterEntry(entry: Entry): Boolean = {
    val request = entry.request
    val method  = request.method
    val url     = request.url
    val status  = entry.response.status
    import PartialFunction.condOpt

    Seq(
      condOpt(statusMin → statusMax) {
        case (Some(min), Some(max)) ⇒
          min to max contains status
        case (None, Some(max)) ⇒
          status <= max
        case (Some(min), None) ⇒
          status >= min
      },
      condOpt(urlFilterRegex) {
        case Some(regex) ⇒
          regex.findFirstIn(url).nonEmpty
      },
      condOpt(methodFilterRegex) {
        case Some(regex) ⇒
          regex.findFirstIn(method).nonEmpty
      }
    ).flatten.forall(_ == true)
  }

  def modifyResponses(httpArchive: Json): Json = {
    if (emptyResponse) {
      Function.chain[Json](
        Seq(
          circe.emptyResponseText,
          circe.emptyResponseHeaders,
          circe.emptyResponseCookies
        ))(httpArchive)
    } else {
      httpArchive
    }
  }
}
