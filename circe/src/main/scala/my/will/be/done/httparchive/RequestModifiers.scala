package my.will.be.done.httparchive.circe

import io.circe.optics.JsonPath._
import io.circe.Json
import io.circe.generic.auto._
import my.will.be.done.httparchive.model.{Entry, Header}

trait RequestModifiers {
  val entriesPath = root.log.entries
  val requestPath = entriesPath.each.request
  def modifyRequestUrls(httpArchive: Json, modifyUrl: String ⇒ String): Json = {
    requestPath.url.string.modify(modifyUrl)(httpArchive)
  }

  def modifyRequestHeaders(httpArchive: Json,
                           modifyHeader: Header ⇒ Header): Json = {
    requestPath.headers.each.as[Header].modify(modifyHeader)(httpArchive)
  }

  def filterEntrys(httpArchive: Json, entryFilter: Entry ⇒ Boolean): Json = {
    entriesPath.as[Seq[Entry]].modify(_.filter(entryFilter))(httpArchive)
  }
}
