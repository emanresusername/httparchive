package my.will.be.done.httparchive.cli

import enumeratum._, EnumEntry._

sealed trait Command extends EnumEntry with Hyphencase

object Command extends Enum[Command] {
  val values = findValues

  case object Replay extends Command
  case object Modify extends Command
}
