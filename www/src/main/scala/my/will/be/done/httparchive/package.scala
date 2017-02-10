package my.will.be.done.httparchive

import my.will.be.done.httparchive.model.TimedResponse.RequestResponse
import org.scalajs.dom.raw.XMLHttpRequest
import org.scalajs.dom.ext.Ajax

package object www {
  implicit val jsRequestResponse: RequestResponse[XMLHttpRequest] = { request ⇒
    Ajax(
      method = request.method,
      headers = request.headers.map { header ⇒
        header.name → header.value
      }.toMap,
      url = request.url,
      data = request.postData.map(_.text).orNull.asInstanceOf[String],
      timeout = 0,
      withCredentials = false,
      responseType = ""
    )
  }
}
