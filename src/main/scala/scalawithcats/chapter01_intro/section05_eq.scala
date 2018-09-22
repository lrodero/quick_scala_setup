package scalawithcats.chapter01_intro

import cats.Eq
import cats.instances.int._ // for Eq[Int]
import cats.instances.long._ // for Eq[Long]
import cats.instances.option._ // for Eq[Option]
import cats.instances.string._ // for Eq[String]
import cats.syntax.eq._ // for === and =!=

import java.util.Date

object Main_1_5 extends App {

  println(123 === 456)
  println(123 =!= 456)

  println(Option(123) === Some(123))
  println(Option.empty[Int] === None)

  implicit val eqDate: Eq[Date] = new Eq[Date] {
    override def eqv(d1: Date, d2: Date): Boolean =
      d1.getTime === d2.getTime
  }

  val d1 = new Date()
  val d2 = {Thread.sleep(10); new Date()}

  println(d1 === d1)
  println(d1 === d2)

  final case class Cat(name: String, age: Int, color: String)

  /*
  implicit val eqCat: Eq[Cat] = new Eq[Cat] {
    override def eqv(c1: Cat, c2: Cat): Boolean =
      c1.name === c2.name && c1.age === c2.age && c1.color === c2.color
  }
  */

  implicit val eqCat = Eq.instance[Cat] { (c1: Cat, c2: Cat) =>
    c1.name === c2.name && c1.age === c2.age && c1.color === c2.color
  }


  val garfield = Cat("garfield", 38, "orange & black")
  val heathcliff = Cat("heathcliff", 38, "orange & black")

  println(garfield === heathcliff)
  println(heathcliff === heathcliff)

  val garfieldO = Option(garfield)
  val noCatO = Option.empty[Cat]

  println(noCatO === None)
  println(garfieldO === Some(garfield))


}

