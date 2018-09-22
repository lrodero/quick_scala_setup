package scalawithcats.chapter02_monoids_and_semigroups

import cats.Monoid
import cats.Eq
import cats.instances.string._ // for Monoid[String], Eq[String]
import cats.instances.int._ // for Monoid[Int]
import cats.instances.option._ // for Monoid[Option]
import cats.syntax.eq._ // for ===
import cats.syntax.semigroup._ // for |+|

object Main_2_5 extends App {

  println(Monoid[String].combine("hello ", "world"))
  println(Monoid[String].empty)
  println(Monoid[String].empty === "")

  println("\n--- Monoid[Option] ---")
  println(Monoid[Option[Int]].combine(None, Option(3)))
  println(Monoid[Option[Int]].combine(Option.empty[Int], Option(3)))
  println(Monoid[Option[Int]].empty === Option.empty[Int])
  println(Monoid[Option[Int]].combine(Option(5), Option(3)))

  println("\n--- Semigroup |+| syntax ---")
  println((Option(3) |+| Option(4)) === (Option(7) |+| None))

  println("\n--- Exercise ---")
  def add(items: List[Int]): Int = items.sum

  def add[A: Monoid](items: List[A]): A = {
    items.foldRight(Monoid[A].empty)(_ |+| _)
  }

  println(add(Option(1) :: None :: Option(100) :: Nil))
  println(add(List.empty[Option[Int]]))

}

