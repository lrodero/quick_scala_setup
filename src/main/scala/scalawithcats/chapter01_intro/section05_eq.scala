package scalawithcats.chapter01_intro

import cats.Eq
import cats.instances.int._ // for Eq[Int]
import cats.instances.option._ // for Eq[Option]
import cats.syntax.eq._ // for === and =!=

object Main_1_5 extends App {

  println(123 === 456)
  println(123 =!= 456)

  println(Option(123) === Some(123))
  println(Option.empty[Int] === None)

}

