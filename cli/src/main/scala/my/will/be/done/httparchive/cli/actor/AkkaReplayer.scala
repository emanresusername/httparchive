package my.will.be.done.httparchive.cli.actor

import my.will.be.done.httparchive.akka.EntryActor
import akka.actor.{Status, Actor, ActorLogging, PoisonPill, ActorRef, Props}
import my.will.be.done.httparchive.model.{HttpArchive}
import scala.concurrent.Future
import CliReplayer.Message.{Replay, ReplayDone}
import java.time.Duration.between
import scala.concurrent.duration._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.agent.Agent

class AkkaReplayer(httpArchive: HttpArchive, watcher: ActorRef)
    extends Actor
    with ActorLogging {
  val system = context.system
  import system.dispatcher
  val scheduler = system.scheduler
  final implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system))

  val requestsLeft = Agent(httpArchive.log.entries.length)

  override def preStart(): Unit = {
    val firstStarted = httpArchive.entry(0).startedInstant
    for {
      (entry, index) ← httpArchive.entrysWithIndex
      delay = between(firstStarted, entry.startedInstant).toMillis.millis
    } {
      scheduler.scheduleOnce(delay) {
        val entryActor = context.actorOf(Props[EntryActor], s"entry-$index")
        entryActor ! EntryActor.Message.Replay(index, entry)
        entryActor ! PoisonPill
      }
    }
  }

  def receive = {
    case failure: Status.Failure ⇒
      watcher ! failure
    case EntryActor.Message.RequestResponse(index, request, response) ⇒
      val httpResponse = response.response
      watcher ! Replay(
        index = index,
        response = response,
        request = request,
        status = httpResponse.status.intValue,
        responseSize = httpResponse.entity.contentLengthOption
      )
      httpResponse.discardEntityBytes()
      for {
        remaining ← requestsLeft.alter(_ - 1)
        if remaining <= 0
      } {
        watcher ! ReplayDone
      }
  }
}
