package my.will.be.done.httparchive.cli

import my.will.be.done.httparchive.cli.actor._
import my.will.be.done.httparchive.Rapture
import akka.actor.{ActorSystem, Props}

object Cli extends App {
  OptionParser.parse(args, Conf()) match {
    case Some(conf) ⇒
      val system = ActorSystem("replay-http-archive")
      val cliReplayer =
        system.actorOf(Props(classOf[CliReplayer], conf), "cli")
      cliReplayer ! Rapture.load(conf.file)
    case None ⇒
  }
}
