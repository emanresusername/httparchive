package my.will.be.done.httparchive.model

// https://dvcs.w3.org/hg/webperf/raw-file/tip/specs/HAR/Overview.html#sec-har

case class HttpArchive(
    log: Log
) {
  def entry(index: Int): Entry = {
    log.entry(index)
  }

  def entrysWithIndex: Seq[(Entry, Int)] = {
    log.entrysWithIndex
  }
}

case class Log(
    version: String,
    creator: Creator,
    browser: Option[Browser],
    pages: Option[Seq[Page]],
    entries: Seq[Entry],
    comment: Option[String]
) {
  def entry(index: Int): Entry = {
    entries(index)
  }

  def entrysWithIndex: Seq[(Entry, Int)] = {
    entries.zipWithIndex
  }
}

case class Creator(
    name: String,
    version: String,
    comment: Option[String]
)

case class Browser(
    name: String,
    version: String,
    comment: Option[String]
)

case class Page(
    startedDateTime: String,
    id: String,
    title: String,
    pageTimings: PageTimings,
    comments: Option[String]
) extends StartedDateTime

case class PageTimings(
    onContentLoad: Option[Long],
    onLoad: Option[Long],
    comment: Option[String]
)

case class Request(
    method: String,
    url: String,
    httpVersion: String,
    cookies: Seq[Cookie],
    headers: Seq[Header],
    queryString: Seq[QueryParameter],
    postData: Option[PostData],
    headersSize: Long,
    bodySize: Long,
    comment: Option[String]
)

case class Response(
    status: Int,
    statusText: String,
    httpVersion: String,
    cookies: Seq[Cookie],
    headers: Seq[Header],
    content: Content,
    redirectURL: String,
    headersSize: Long,
    bodySize: Long,
    comment: Option[String]
)

case class Cookie(
    name: String,
    value: String,
    path: Option[String],
    domain: Option[String],
    expires: Option[String],
    httpOnly: Option[Boolean],
    secure: Option[Boolean],
    comment: Option[String]
)

case class Header(
    name: String,
    value: String,
    comment: Option[String]
)

case class QueryParameter(
    name: String,
    value: String,
    comment: Option[String]
)

case class PostData(
    mimeType: String,
    params: Seq[PostParameter],
    text: String,
    comment: Option[String]
)

case class PostParameter(
    name: String,
    value: Option[String],
    fileName: Option[String],
    contentType: Option[String],
    comment: Option[String]
)

case class Content(
    size: Long,
    compression: Option[Long],
    mimeType: String,
    text: Option[String],
    encoding: Option[String],
    comment: Option[String]
)

case class Cache(
    beforeRequest: Option[AroundRequest],
    afterRequest: Option[AroundRequest],
    comment: Option[String]
)

case class AroundRequest(
    expires: Option[String],
    lastAccess: String,
    eTag: String,
    hitCount: Long,
    comment: Option[String]
)

case class Timings(
    blocked: Option[Long],
    dns: Option[Long],
    connect: Option[Long],
    send: Long,
    // TODO: overriding method wait in class Object of type ()Unit; value wait cannot override final member
    // wait: Long,
    receive: Long,
    ssl: Option[Long],
    comment: Option[String]
)
