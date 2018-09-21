package scalawithcats.chapter01_intro

sealed trait Json
final case class JsObject(get: Map[String, Json]) extends Json
final case class JsString(get: String) extends Json
final case class JsNumber(get: Double) extends Json
case object JsNull extends Json

trait JsonWriter[A] {
  def write(value: A): Json
}

final case class Person(name: String, email: String)

object JsonWriter {
  implicit val stringWriter = new JsonWriter[String] {
    override def write(value: String): Json = JsString(value)
  }

  implicit val personWriter = new JsonWriter[Person] {
    override def write(value: Person): Json = JsObject( Map(
      ("name" -> JsString(value.name)),
      ("email" -> JsString(value.email))
    ))
  }

  implicit class JsonWriterOps[A: JsonWriter](a: A) {
    def toJson: Json = JsonWriter.toJson(a)
  }

  implicit def opJsonWriter[A: JsonWriter]: JsonWriter[Option[A]] = new JsonWriter[Option[A]] {
    override def write(oa: Option[A]): Json = 
      oa match {
        case None => JsNull
        case Some(a) => a.toJson
      }
  }

  implicit def toJson[A: JsonWriter](a: A): Json =
    implicitly[JsonWriter[A]].write(a)

}


object Main extends App {
}
