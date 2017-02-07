package my.will.be.done.httparchive.cli.actor

import my.will.be.done.httparchive.circe.loadHttpArchive
import my.will.be.done.httparchive.Rapture
import my.will.be.done.httparchive.cli.Conf
import my.will.be.done.httparchive.model.{Request, TimedResponse, HttpArchive}
import java.time.Instant
import akka.actor.{Actor, ActorLogging, Props, Status, PoisonPill}
import CliReplayer.Message.Replay
import CliReplayer.IndexRequestResponse
import scala.util.Try
import io.circe.syntax._
import io.circe.generic.auto._
import scala.concurrent.duration.{MILLISECONDS, Duration}

class CliReplayer(conf: Conf) extends Actor with ActorLogging {
  import context.actorOf

  val system = context.system

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

  override def preStart(): Unit = {
    (for {
      httpArchiveOrError ← Try { loadHttpArchive(conf.file) }.toEither
      httpArchive        ← httpArchiveOrError
    } yield {
      httpArchive
    }) match {
      case Left(cause) ⇒
        log.error(cause, "couldn't load http archive, shutting down")
        self ! PoisonPill
      case Right(httpArchive) ⇒
        self ! httpArchive
    }
  }

  override def postStop(): Unit = {
    system.terminate
  }

  def httpArchiveLoaded(httpArchive: HttpArchive): Receive = {
    if (conf.serial) {
      actorOf(Props[RaptureReplayer], "rapture-replayer") ! httpArchive
    } else {
      actorOf(Props(classOf[AkkaReplayer], httpArchive, self), "akka-replayer")
    }

    {
      case Status.Failure(cause) ⇒
        log.error(cause, "recieved failure from {}", sender)
      case replay: CliReplayer.Message.Replay ⇒
        val request = httpArchive.entry(replay.index).request
        val printReplay = if (conf.jsonline) {
          jsonPrintln _
        } else {
          tsvPrintln _
        }
        printReplay(IndexRequestResponse(replay = replay, request = request))
      case CliReplayer.Message.ReplayDone ⇒
        log.debug("replay finished, shutting system down")
        context.unbecome
        self ! PoisonPill
    }
  }

  def receive = {
    case httpArchive: HttpArchive ⇒
      context.become(httpArchiveLoaded(httpArchive))
  }
}

object CliReplayer {
  case class IndexRequestResponse(
      index: Int,
      startMillis: Long,
      endMillis: Long,
      status: Int,
      responseSize: Option[Long],
      requestSize: Long,
      uri: String,
      method: String
  ) {
    def startInstant: Instant = {
      Instant.EPOCH.plusMillis(startMillis)
    }

    def duration: Duration = {
      Duration(endMillis - startMillis, MILLISECONDS)
    }
  }

  object IndexRequestResponse {
    def apply(request: Request, replay: Replay): IndexRequestResponse = {
      IndexRequestResponse(
        startMillis = replay.startMillis,
        endMillis = replay.endMillis,
        index = replay.index,
        responseSize = replay.responseSize,
        status = replay.status,
        method = request.method,
        uri = request.url,
        requestSize = request.bodySize
      )
    }
  }

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
