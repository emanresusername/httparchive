package my.will.be.done.httparchive.model

import java.time.Instant
import java.text.SimpleDateFormat

trait StartedDateTime {
  val startedDateTime: String
  def startedInstant: Instant = {
    StartedDateTime.startedInstant(startedDateTime)
  }
}

object StartedDateTime {
  val StartedDateTimeFormatter =
    new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSXXX")
  def startedInstant(string: String): Instant = {
    StartedDateTimeFormatter.parse(string).toInstant
  }
}
