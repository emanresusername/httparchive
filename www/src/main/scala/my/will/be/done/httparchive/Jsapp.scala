package my.will.be.done.httparchive.www

import scala.scalajs.js
import my.will.be.done.httparchive.model.TimedResponse
import org.scalajs.dom.document
import org.scalajs.dom.window.console
import my.will.be.done.httparchive.model.HttpArchive
import my.will.be.done.httparchive.circe
import org.scalajs.dom.raw.{HTMLInputElement, Event, FileReader, File}
import org.scalajs.dom.ext.AjaxException
import com.thoughtworks.binding.Binding._
import scala.concurrent.{Promise, Future}
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import com.thoughtworks.binding.dom

object Jsapp extends js.JSApp {
  val httpArchives = Vars.empty[HttpArchive]

  def loadFile(file: File): Future[HttpArchive] = {
    val fileReader = new FileReader()
    val promise    = Promise[HttpArchive]()
    fileReader.onload = { event ⇒
      event.target match {
        case `fileReader` ⇒
          circe.loadHttpArchive(
            fileReader.result.asInstanceOf[String]
          ) match {
            case Left(error) ⇒
              promise.failure(error)
            case Right(httpArchive) ⇒
              promise.success(httpArchive)
          }
      }
    }
    fileReader.readAsText(file)
    promise.future
  }

  val loadFiles = { event: Event ⇒
    event.currentTarget match {
      case input: HTMLInputElement ⇒
        val files = input.files
        for {
          index ← 0 until files.length
          file = files(index)
          httpArchive ← loadFile(file)
        } {
          httpArchives.get += httpArchive
        }
    }
  }

  val replayHttpArchives = { event: Event ⇒
    for {
      httpArchive ← httpArchives.get
      entry       ← httpArchive.log.entries
      request = entry.request
    } {
      TimedResponse(request).onComplete {
        case util.Success(response) ⇒
          console.log(response.response)
        case util.Failure(AjaxException(xmlHttpRequest)) ⇒
          console.error(xmlHttpRequest)
      }
    }
  }

  @dom
  def render = {
    <div>
      <input type="file" onchange={loadFiles}></input>
      <button onclick={replayHttpArchives}>Replay</button>
      </div>
  }

  def main(): Unit = {
    dom.render(document.body, render)
  }
}
