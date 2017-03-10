package my.will.be.done.httparchive.cli.actor

import my.will.be.done.httparchive.rapture
import my.will.be.done.httparchive.cli.Conf
import my.will.be.done.httparchive.model.{Request, TimedResponse, HttpArchive}
import java.time.Instant
import akka.actor.{Actor, ActorLogging, Props, Status, PoisonPill, ActorRef}
import CliReplayer.Message.Replay
import CliReplayer.IndexRequestResponse
import scala.concurrent.duration.{MILLISECONDS, Duration}

class CliReplayer(conf: Conf, printer: ActorRef)
    extends Actor
    with ActorLogging {
  import context.actorOf

  val system = context.system

  def receive = {
    case httpArchive: HttpArchive ⇒
      if (conf.serial) {
        actorOf(Props[RaptureReplayer], "rapture-replayer") ! httpArchive
      } else {
        actorOf(Props(classOf[AkkaReplayer], httpArchive, self),
                "akka-replayer")
      }
    case Status.Failure(cause) ⇒
      log.error(cause, "recieved failure from {}", sender)
    case replay: CliReplayer.Message.Replay ⇒
      printer ! replay
    case CliReplayer.Message.ReplayDone ⇒
      log.debug("replay finished, taking poison pill")
      self ! PoisonPill
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
