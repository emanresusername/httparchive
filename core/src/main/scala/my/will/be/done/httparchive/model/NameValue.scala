package my.will.be.done.httparchive.model

case class NameValue(name: String, value: String)

object NameValue {
  def map(nameValues: Seq[NameValue]): Map[String, String] = {
    nameValues.map {
      case NameValue(name, value) ⇒
        name → value
    }.toMap
  }
}
