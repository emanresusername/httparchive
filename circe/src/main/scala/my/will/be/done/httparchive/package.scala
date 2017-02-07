package my.will.be.done.httparchive

import io.circe.{Json, Error}
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.optics.JsonPath._
import scala.io.Source
import java.io.File
import my.will.be.done.httparchive.model.HttpArchive

package object circe {
  def loadJson(string: String): Either[Error, Json] = {
    parse(string)
  }

  def loadJson(file: File): Either[Error, Json] = {
    loadJson(Source.fromFile(file).mkString)
  }

  def loadHttpArchive(json: Json): Either[Error, HttpArchive] = {
    json.as[HttpArchive]
  }

  def loadHttpArchive(string: String): Either[Error, HttpArchive] = {
    for {
      json        ← loadJson(string)
      httpArchive ← loadHttpArchive(json)
    } yield {
      httpArchive
    }
  }

  def loadHttpArchive(file: File): Either[Error, HttpArchive] = {
    for {
      json        ← loadJson(file)
      httpArchive ← loadHttpArchive(json)
    } yield {
      httpArchive
    }
  }
}
