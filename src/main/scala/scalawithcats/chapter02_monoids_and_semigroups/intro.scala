package scalawithcats.chapter02_monoids_and_semigroups

import cats.Eq
import cats.syntax.eq._


object Intro extends App {
  trait Semigroup[A] {
    def combine(a1: A, a2: A): A
  }

  trait Monoid[A] extends Semigroup[A] {
    def empty: A
  }

  object MonoidLaws {

    def associativeLaw[A: Monoid: Eq](a1: A, a2: A, a3: A) = {
      val ma = implicitly[Monoid[A]]
      ma.combine(ma.combine(a1, a2), a3) === ma.combine(a1, ma.combine(a2, a3))
    }

    def identityLaw[A: Monoid: Eq](a: A) = {
      val ma = implicitly[Monoid[A]]
      ma.combine(a, ma.empty) === a && ma.combine(ma.empty, a) === a
    }

  }

  object Monoid {

    object instances {
      implicit val boolMonoidAnd: Monoid[Boolean] = new Monoid[Boolean] {
        override def combine(b1: Boolean, b2: Boolean): Boolean = b1 && b2
        override def empty: Boolean = true
      }
      implicit val boolMonoidOr: Monoid[Boolean] = new Monoid[Boolean] {
        override def combine(b1: Boolean, b2: Boolean) = b1 || b2
        override def empty: Boolean = false
      }
    }

    def apply[A: Monoid]: Monoid[A] = implicitly[Monoid[A]]

  }
}

