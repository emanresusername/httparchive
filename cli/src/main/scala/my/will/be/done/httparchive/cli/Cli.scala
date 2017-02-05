package my.will.be.done.httparchive.cli

import my.will.be.done.httparchive.Rapture
import scala.concurrent.duration.MILLISECONDS
import java.time.Instant.EPOCH

object Cli extends App {
  OptionParser.parse(args, Conf()) match {
    case Some(conf) ⇒
      println(
        Seq(
          "timestamp",
          "status",
          "method",
          "url",
          "duration"
        ).mkString("\t"))
      for {
        (entry, response) ← Rapture.replay(Rapture.load(conf.file))
        startMillis = response.startMillis
        endMillis   = response.endMillis
        duration    = endMillis - startMillis
        request     = entry.request
      } {
        println(
          Seq(
            EPOCH.plusMillis(startMillis),
            response.response.status,
            request.method,
            request.url,
            s"$duration ms"
          ).mkString("\t"))
      }
    case None ⇒
  }
}
