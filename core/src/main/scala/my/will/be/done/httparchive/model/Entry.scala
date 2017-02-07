package my.will.be.done.httparchive.model

case class Entry(
    pageref: Option[String],
    startedDateTime: String,
    time: Long,
    request: Request,
    response: Response,
    cache: Cache,
    timings: Timings,
    serverIPAddress: Option[String],
    connection: Option[String],
    comment: Option[String]
) extends StartedDateTime
