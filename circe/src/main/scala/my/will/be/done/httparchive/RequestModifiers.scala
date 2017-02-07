package my.will.be.done.httparchive.circe

import io.circe.optics.JsonPath._
import io.circe.Json
import io.circe.generic.auto._
import my.will.be.done.httparchive.model.Header

trait RequestModifiers {
  val requestPath = root.log.entries.each.request
  def modifyRequestUrls(httpArchive: Json, modifyUrl: String ⇒ String): Json = {
    requestPath.url.string.modify(modifyUrl)(httpArchive)
  }

  def modifyRequestHeaders(httpArchive: Json,
                           modifyHeader: Header ⇒ Header): Json = {
    requestPath.headers.each.as[Header].modify(modifyHeader)(httpArchive)
  }
}
