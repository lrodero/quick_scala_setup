package scalawithcats.chapter01_intro

import cats.Show
import cats.instances.int._ // for Show[Int]
import cats.instances.string._ // for Show[String]
import cats.syntax.show._ // for show

import java.util.Date

object Main_1_4 extends App {

  val showInt = Show[Int]
  val showString = Show[String]

  showInt.show(123)
  showString.show("hi")
  1234.show
  "hello".show

  val showDate = new Show[Date] {
    override def show(d: Date): String =
      s"${d.getTime}ms since the epoch."
  }

  val showDate2 = Show.show[Date]{ (d: Date) =>
    s"${d.getTime}ms since the epoch."
  }

  val showDate3 = Show.fromToString[Date]

  final case class Cat(name: String, age: Int, color: String)

  implicit val catShow = Show.show { (c: Cat) =>
    s"${c.name.show} is a ${c.age.show} years-old ${c.color.show} cat"
  }

  implicit class ShowPrintOp[A: Show](a: A) {
    def print(): Unit = println(a.show)
  }

  Cat("garfield", 38, "orange and black").print

}

