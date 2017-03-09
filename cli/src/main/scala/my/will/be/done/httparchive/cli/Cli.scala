package my.will.be.done.httparchive.cli

import my.will.be.done.httparchive.cli.actor._
import my.will.be.done.httparchive.rapture
import my.will.be.done.httparchive.circe
import akka.actor.{ActorSystem, Props, Terminated, ActorDSL}
import my.will.be.done.httparchive.cli.Command._
import my.will.be.done.httparchive.circe.loadHttpArchive
import scala.util.Try
import scala.concurrent.duration._

object Cli extends App {
  OptionParser.parse(args, Conf()) match {
    case Some(conf) ⇒
      conf.command match {
        case Replay ⇒
          implicit val system = ActorSystem("replay-http-archive")
          val log             = system.log
          val actors          = conf.actors
          val inbox           = ActorDSL.inbox()
          (for {
            httpArchiveOrError ← Try { loadHttpArchive(conf.file) }.toEither
            httpArchive        ← httpArchiveOrError
          } yield {
            httpArchive
          }) match {
            case Left(cause) ⇒
              log.error(cause, "couldn't load http archive, shutting down")
              system.terminate
            case Right(httpArchive) ⇒
              for {
                index ← 0 until actors
                actorName = s"cli-$index"
              } {
                log.info(s"starting actor $actorName")
                val actor =
                  system.actorOf(Props(classOf[CliReplayer], conf), actorName)
                inbox.watch(actor)
                actor ! httpArchive
              }
          }
          Stream
            .continually(inbox.select(1.hour) {
              case Terminated(actor) ⇒
                log.info("terminated {}", actor)
            })
            .take(actors)
            .force
          system.terminate()
        case Modify ⇒
          for {
            httpArchive ← circe.loadJson(conf.file)
            modifiedUrls = circe.modifyRequestUrls(
              httpArchive,
              conf.modifyUrl
            )
            modified = circe.modifyRequestHeaders(
              modifiedUrls,
              conf.modifyUrlHeaderValue
            )
          } {
            conf.outputStream.print(
              (
                if (conf.emptyResponse) {
                  circe.emptyResponseText(
                    circe.emptyResponseHeaders(
                      circe.emptyResponseCookies(
                        modified
                      )
                    )
                  )
                } else {
                  modified
                }
              ).noSpaces)
          }
      }
    case None ⇒
  }
}
