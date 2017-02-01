package my.will.be.done.httparchive

import java.io.File
import my.will.be.done.httparchive.model._

import rapture.core.EnrichedString
import rapture.uri._
import rapture.io._
import rapture.net._
import rapture.codec._, encodings.`UTF-8`._
import rapture.json.jsonBackends.circe._
import rapture.json.Json
import scala.io.Source
import java.io.File
import scala.concurrent.duration._
import System.nanoTime

case class Replay(query: HttpQuery,
                  method: String,
                  response: HttpResponse,
                  duration: Duration)

object Replay {
  def apply(file: File): Iterator[Replay] = {
    Replay(Source.fromFile(file).mkString)
  }

  def apply(string: String): Iterator[Replay] = {
    Replay(Json.parse(string))
  }

  def request(request: Json): Replay = {
    val method  = request.method.as[String].toUpperCase
    val query   = request.url.as[String].as[HttpQuery]
    val headers = NameValue.map(request.headers.as[Seq[NameValue]])

    def body = request.postData.text.as[String]

    val startTime = nanoTime
    val response = method match {
      case "GET" ⇒
        query.httpGet(headers)
      case "POST" ⇒
        query.httpPost(body, headers)
      case "PUT" ⇒
        query.httpPut(body, headers)
      case "OPTIONS" ⇒
        query.httpOptions(headers)
      case "DELETE" ⇒
        query.httpDelete(headers)
      case "HEAD" ⇒
        query.httpHead(headers)
      case "TRACE" ⇒
        query.httpTrace(headers)
    }
    val endTime = nanoTime

    Replay(
      query = query,
      method = method,
      response = response,
      duration = (endTime - startTime).nanos
    )
  }

  def apply(json: Json): Iterator[Replay] = {
    for {
      entry ← json.log.entries.as[Seq[Json]].iterator
    } yield {
      Replay.request(entry.request)
    }
  }
}
