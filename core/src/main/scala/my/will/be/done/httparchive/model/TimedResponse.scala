package my.will.be.done.httparchive.model

import scala.concurrent.{Future, ExecutionContext}
import System.currentTimeMillis

case class TimedResponse[R](
    response: R,
    startMillis: Long,
    endMillis: Long
)

object TimedResponse {
  type RequestResponse[R] = Entry ⇒ Future[R]

  def apply[R](entry: Entry)(
      implicit requestResponse: RequestResponse[R],
      executioContext: ExecutionContext): Future[TimedResponse[R]] = {
    val startMillis = currentTimeMillis
    for {
      response ← requestResponse(entry)
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
    : Iterator[Future[(Entry, TimedResponse[R])]] = {
    for {
      entry ← httpArchive.log.entries.toIterator
    } yield {
      TimedResponse(entry).map(entry → _)
    }
  }
}
