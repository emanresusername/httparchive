package my.will.be.done.httparchive.cli

import my.will.be.done.httparchive.Replay
import scala.concurrent.duration.SECONDS

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
        replay ← Replay(conf.file)
        duration = replay.duration
      } {
        println(
          Seq(
            java.time.Instant.now,
            replay.response.status,
            replay.method,
            replay.query.toString,
            duration.toUnit(SECONDS)
          ).mkString("\t"))
      }
    case None ⇒
  }
}
