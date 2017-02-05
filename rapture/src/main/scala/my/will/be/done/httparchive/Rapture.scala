package my.will.be.done.httparchive

import java.io.File
import my.will.be.done.httparchive.model._, TimedResponse.RequestResponse

import rapture.core.EnrichedString
import rapture.uri._
import rapture.io._
import rapture.net._
import rapture.codec._, encodings.`UTF-8`._
import rapture.json.jsonBackends.circe._
import rapture.json.Json
import scala.io.Source
import java.io.File
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object Rapture {
  implicit val raptureRequestResponse: RequestResponse[HttpResponse] = {
    entry ⇒
      val request = entry.request
      val headers = request.headers.map { header ⇒
        header.name → header.value
      }.toMap
      val query = request.url.as[HttpQuery]

      def body = request.postData.map(_.text).getOrElse("")
      Future {
        request.method.toUpperCase match {
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
      }
  }

  def replay(httpArchive: HttpArchive)
    : Iterator[(Entry, TimedResponse[HttpResponse])] = {
    for {
      futureEntryResponse ← TimedResponse(httpArchive)
    } yield {
      // TODO: async using entry.startDateTime
      Await.result(futureEntryResponse, Duration.Inf)
    }
  }

  def load(json: Json): HttpArchive = {
    json.as[HttpArchive]
  }

  def load(string: String): HttpArchive = {
    load(Json.parse(string))
  }

  def load(file: File): HttpArchive = {
    load(Source.fromFile(file).mkString)
  }

}
