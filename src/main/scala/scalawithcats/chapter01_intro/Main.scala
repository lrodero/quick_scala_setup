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
}

object Main extends App {
}
