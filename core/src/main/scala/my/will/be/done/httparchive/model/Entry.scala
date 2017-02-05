package my.will.be.done.httparchive.model

import java.time.Instant
import java.text.SimpleDateFormat

case class Entry(
    pageref: Option[String],
    startedDateTime: String,
    time: Long,
    request: Request,
    response: Response,
    cache: Cache,
    timings: Timings,
    serverIPAddress: Option[String],
    connection: Option[String],
    comment: Option[String]
) {
  def startedInstant: Instant = {
    Entry.startedInstant(startedDateTime)
  }
}

object Entry {
  val StartedDateTimeFormatter =
    new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSXXX")
  def startedInstant(string: String): Instant = {
    StartedDateTimeFormatter.parse(string).toInstant
  }
}
