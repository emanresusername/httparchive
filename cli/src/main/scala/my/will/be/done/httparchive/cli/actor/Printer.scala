package my.will.be.done.httparchive.cli.actor

import akka.actor.{Actor, ActorLogging}
import io.circe.syntax._
import io.circe.generic.auto._
import CliReplayer.IndexRequestResponse
import my.will.be.done.httparchive.model.HttpArchive
import my.will.be.done.httparchive.cli.Conf

class Printer(conf: Conf, httpArchive: HttpArchive)
    extends Actor
    with ActorLogging {

  // TODO: akka streams file sink or something
  lazy val printStream = synchronized {
    conf.outputStream
  }

  def tsvPrintln(replay: IndexRequestResponse): Unit = {
    printStream.println(
      Seq(
        replay.index,
        replay.startInstant,
        replay.status,
        replay.method,
        replay.uri,
        replay.duration.toMillis,
        replay.responseSize,
        replay.requestSize
      ).mkString(conf.tsvDelimiter)
    )
  }

  def jsonPrintln(replay: IndexRequestResponse): Unit = {
    printStream.println(replay.asJson.noSpaces)
  }

  def receive = {
    case replay: CliReplayer.Message.Replay ⇒
      val request = httpArchive.entry(replay.index).request
      val printReplay = if (conf.jsonline) {
        jsonPrintln _
      } else {
        tsvPrintln _
      }
      printReplay(IndexRequestResponse(replay = replay, request = request))
  }
}
