package my.will.be.done.httparchive.cli

import my.will.be.done.httparchive.cli.actor._
import my.will.be.done.httparchive.Rapture
import my.will.be.done.httparchive.circe
import akka.actor.{ActorSystem, Props}
import my.will.be.done.httparchive.cli.Command._

object Cli extends App {
  OptionParser.parse(args, Conf()) match {
    case Some(conf) ⇒
      conf.command match {
        case Replay ⇒
          val system = ActorSystem("replay-http-archive")
          val cliReplayer =
            system.actorOf(Props(classOf[CliReplayer], conf), "cli")
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
