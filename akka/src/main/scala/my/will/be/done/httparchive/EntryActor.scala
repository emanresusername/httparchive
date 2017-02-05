package my.will.be.done.httparchive.akka

import akka.actor.{Actor, ActorLogging}
import my.will.be.done.httparchive.model._, TimedResponse.RequestResponse
import akka.http.scaladsl.model._, HttpHeader.ParsingResult
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import EntryActor.Message
import scala.util.{Try, Success, Failure}

class EntryActor extends Actor with ActorLogging {
  val system = context.system
  import system.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system))

  val http      = Http(system)
  val scheduler = system.scheduler

  /**
    * TODO: configurable conditions for failure scenarios
    */
  implicit val akkaRequestResponse: RequestResponse[HttpResponse] = { entry ⇒
    val request = entry.request
    val headerOrErrors = EntryActor.httpHeaders(request).collect {
      case ParsingResult.Ok(header, errors) ⇒
        for {
          error ← errors
        } {
          log.warning("survivable error parsing header: {}", error)
        }
        Right(header)
      case ParsingResult.Error(error) ⇒
        log.error("fatal error parsing header: {}", error)
        Left(error)
    }

    val headersTry = headerOrErrors.partition(_.isLeft) match {
      case (Nil, rights) ⇒
        Success(rights.flatMap(_.right.toOption))
      case (lefts, _) ⇒
        Failure(EntryActor.HeaderParsingErrors(lefts.flatMap(_.left.toOption)))
    }

    for {
      headers ← Future.fromTry(headersTry)
      method  ← Future.fromTry(EntryActor.httpMethod(request))
      entity  ← Future.fromTry(EntryActor.httpEntity(request))
      httpRequest = HttpRequest(uri = request.url,
                                headers = headers.toList,
                                method = method,
                                entity = entity)
      response ← http.singleRequest(httpRequest)
    } yield {
      response
    }
  }

  def receive = {
    case Message.Replay(index, entry) ⇒
      pipe(
        for {
          response ← TimedResponse(entry)
        } yield {
          Message.RequestResponse(index, entry.request, response)
        }
      ).to(sender)
  }
}

object EntryActor {
  case class UnsupportedHttpMethod(method: String)
      extends Exception(s"http method `$method` is not supported")
  case class HeaderParsingErrors(errors: Seq[ErrorInfo])
      extends Exception(s"errors parsing headers: $errors")
  case class MimeTypeParsingErrors(errors: Seq[ErrorInfo])
      extends Exception(s"errors parsing mime type: $errors")

  def httpEntity(request: Request): Try[RequestEntity] = {
    request.postData match {
      case Some(PostData(mimeType, _, text, _)) if mimeType.nonEmpty ⇒
        ContentType.parse(mimeType) match {
          case Left(errors) ⇒
            Failure(MimeTypeParsingErrors(errors))
          case Right(contentType) ⇒
            Success(HttpEntity(contentType, text.getBytes))
        }
      case _ ⇒
        Success(HttpEntity.Empty)
    }
  }

  def httpHeaders(request: Request): Seq[ParsingResult] = {
    for {
      header ← request.headers
    } yield {
      HttpHeader.parse(header.name, header.value)
    }
  }

  def httpMethod(request: Request): Try[HttpMethod] = {
    val rawMethod = request.method
    HttpMethods.getForKey(rawMethod) match {
      case Some(method) ⇒
        Success(method)
      case None ⇒
        Failure(UnsupportedHttpMethod(rawMethod))
    }
  }

  object Message {
    case class Replay(index: Int, entry: Entry)
    case class RequestResponse(index: Int,
                               request: Request,
                               response: TimedResponse[HttpResponse])
  }
}
