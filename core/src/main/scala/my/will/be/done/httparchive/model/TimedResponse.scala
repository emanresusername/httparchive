package my.will.be.done.httparchive.model

import scala.concurrent.{Future, ExecutionContext}
import System.currentTimeMillis

case class TimedResponse[R](
    response: R,
    startMillis: Long,
    endMillis: Long
)

object TimedResponse {
  type RequestResponse[R] = Request ⇒ Future[R]

  def apply[R](request: Request)(
      implicit requestResponse: RequestResponse[R],
      executioContext: ExecutionContext): Future[TimedResponse[R]] = {
    val startMillis = currentTimeMillis
    for {
      response ← requestResponse(request)
    } yield {
      TimedResponse(
        response = response,
        startMillis = startMillis,
        endMillis = currentTimeMillis
      )
    }
  }

  def apply[R](httpArchive: HttpArchive)(
      implicit requestResponse: RequestResponse[R],
      executioContext: ExecutionContext)
    : Iterator[Future[(Int, TimedResponse[R])]] = {
    for {
      (entry, index) ← httpArchive.entrysWithIndex.toIterator
      request = entry.request
    } yield {
      TimedResponse(request).map(index → _)
    }
  }
}
