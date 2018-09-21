package scalawithcats.chapter01_intro

trait Printable[A] {
  def format(a: A): String
}

object Printable {

  object instances {
    implicit val printableString = new Printable[String] {
      override def format(s: String) = s
    }
    implicit val printableInt = new Printable[Int] {
      override def format(i: Int) = i.toString
    }
  }

  implicit def format[A: Printable](a: A): String = 
    a.format

  implicit def print[A: Printable](a: A): Unit =
    a.print

  implicit class PrintableOps[A: Printable](a: A) {
    def format: String = implicitly[Printable[A]].format(a)
    def print(): Unit = println(format)
  }

}

final case class Cat(name: String, age: Int, color: String)

object PrintableCat extends Printable[Cat] {
  import Printable._
  import Printable.instances._
  override def format(c: Cat) = 
    s"${Printable.format(c.name)} is a ${c.age.format} year-old ${Printable.format(c.color)} COLOR cat"
}

object Exercise extends App {

  import Printable._
  implicit val pcat = PrintableCat
  Cat("Garfield", 38, "ginner and black").print

}
