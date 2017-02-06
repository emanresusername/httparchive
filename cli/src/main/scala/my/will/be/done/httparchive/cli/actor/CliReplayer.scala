package my.will.be.done.httparchive.cli.actor

import my.will.be.done.httparchive.Rapture
import my.will.be.done.httparchive.cli.Conf
import my.will.be.done.httparchive.model.{Request, TimedResponse, HttpArchive}
import java.time.Instant.EPOCH
import akka.actor.{Actor, ActorLogging, Props, Status}
import CliReplayer.Message.Replay
import rapture.json.Json
import rapture.json.jsonBackends.circe._
import rapture.json.formatters.compact._

class CliReplayer(conf: Conf) extends Actor with ActorLogging {
  import context.actorOf

  val system      = context.system
  val httpArchive = Rapture.load(conf.file)

  // TODO: akka streams file sink or something
  lazy val printStream = synchronized {
    conf.outputStream
  }

  def tsvPrintln(replay: Replay): Unit = {
    val startMillis = replay.startMillis
    val endMillis   = replay.endMillis
    val duration    = endMillis - startMillis
    val index       = replay.index
    val request     = httpArchive.entry(index).request

    printStream.println(
      Seq(
        index,
        EPOCH.plusMillis(startMillis),
        replay.status,
        request.method,
        request.url,
        duration,
        replay.responseSize,
        request.bodySize
      ).mkString(conf.tsvDelimiter)
    )
  }

  def jsonPrintln(replay: Replay): Unit = {
    printStream.println(Json.format(Json(replay)))
  }

  override def preStart(): Unit = {
    if (conf.serial) {
      actorOf(Props[RaptureReplayer], "rapture-replayer") ! httpArchive
    } else {
      actorOf(Props(classOf[AkkaReplayer], httpArchive, self), "akka-replayer")
    }
  }

  def receive = {
    case Status.Failure(cause) ⇒
      log.error(cause, "recieved failure from {}", sender)
    case replay: CliReplayer.Message.Replay ⇒
      val printReplay = if (conf.jsonline) {
        jsonPrintln _
      } else {
        tsvPrintln _
      }
      printReplay(replay)
    case CliReplayer.Message.ReplayDone ⇒
      log.debug("replay finished, shutting system down")
      system.terminate
  }
}

object CliReplayer {
  object Message {
    case class Replay(
        index: Int,
        startMillis: Long,
        endMillis: Long,
        status: Int,
        responseSize: Option[Long]
    )

    object Replay {
      def apply[R](index: Int,
                   status: Int,
                   responseSize: Option[Long],
                   response: TimedResponse[R]): Replay = {
        Replay(
          index = index,
          status = status,
          responseSize = responseSize,
          startMillis = response.startMillis,
          endMillis = response.endMillis
        )
      }
    }

    case object ReplayDone
  }
}
