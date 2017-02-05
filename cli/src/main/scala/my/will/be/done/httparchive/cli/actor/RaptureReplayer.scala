package my.will.be.done.httparchive.cli.actor

import my.will.be.done.httparchive.Rapture
import akka.actor.{Actor, ActorLogging, PoisonPill}
import my.will.be.done.httparchive.model.{HttpArchive}
import scala.concurrent.Future
import CliReplayer.Message.{Replay, ReplayDone}

class RaptureReplayer extends Actor with ActorLogging {
  val system = context.system
  import system.dispatcher
  import Rapture.raptureRequestResponse

  def receive = {
    case httpArchive: HttpArchive ⇒
      for {
        ((entry, response), index) ← Rapture.replay(httpArchive).zipWithIndex
        request         = entry.request
        raptureResponse = response.response
      } {
        sender ! Replay(
          index = index,
          response = response,
          request = request,
          status = raptureResponse.status,
          responseSize = raptureResponse.headers.collectFirst {
            case (name, List(length))
                if "content-length".equalsIgnoreCase(name) =>
              length.toLong
          }
        )
      }
      sender ! ReplayDone
  }
}
