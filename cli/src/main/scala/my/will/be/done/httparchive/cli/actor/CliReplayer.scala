package my.will.be.done.httparchive.cli.actor

import my.will.be.done.httparchive.circe.loadHttpArchive
import my.will.be.done.httparchive.Rapture
import my.will.be.done.httparchive.cli.Conf
import my.will.be.done.httparchive.model.{Request, TimedResponse, HttpArchive}
import java.time.Instant.EPOCH
import akka.actor.{Actor, ActorLogging, Props, Status, PoisonPill}
import CliReplayer.Message.Replay
import scala.util.Try
import io.circe.syntax._
import io.circe.generic.auto._

class CliReplayer(conf: Conf) extends Actor with ActorLogging {
  import context.actorOf

  val system = context.system
  // TODO: supervisor strategies or something to get this immediately or fail and terminate. http://doc.akka.io/docs/akka/current/general/supervision.html
  val httpArchiveOrError = for {
    either ← Try { loadHttpArchive(conf.file) }.toEither
    right  ← either
  } yield right

  // TODO: akka streams file sink or something
  lazy val printStream = synchronized {
    conf.outputStream
  }

  def tsvPrintln(replay: Replay): Unit = {
    val startMillis = replay.startMillis
    val endMillis   = replay.endMillis
    val duration    = endMillis - startMillis
    val index       = replay.index
    val request     = httpArchiveOrError.right.get.entry(index).request

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
    printStream.println(replay.asJson.noSpaces)
  }

  override def preStart(): Unit = {
    httpArchiveOrError match {
      case Left(cause) ⇒
        log.error(cause, "couldn't load http archive, shutting down")
        self ! PoisonPill
      case Right(httpArchive) ⇒
        if (conf.serial) {
          actorOf(Props[RaptureReplayer], "rapture-replayer") ! httpArchive
        } else {
          actorOf(Props(classOf[AkkaReplayer], httpArchive, self),
                  "akka-replayer")
        }
    }
  }

  override def postStop(): Unit = {
    system.terminate
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
      self ! PoisonPill
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
