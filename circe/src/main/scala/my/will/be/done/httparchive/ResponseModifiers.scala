package my.will.be.done.httparchive.circe

import io.circe.Json
import io.circe.optics.JsonPath._

trait ResponseModifiers {
  val responsePath = root.log.entries.each.response

  def emptyResponseText(httpArchive: Json): Json =
    responsePath.content.text.as[Option[String]].set(None)(httpArchive)

  def emptyResponseHeaders(httpArchive: Json): Json =
    responsePath.headers.as[Seq[Json]].set(Nil)(httpArchive)

  def emptyResponseCookies(httpArchive: Json): Json =
    responsePath.cookies.as[Seq[Json]].set(Nil)(httpArchive)
}
