package org.lrodero.cats_effect_manual.data_types.resource

import cats.effect.{IO, Resource}
import cats.implicits._

object Main extends App {

  def mkResource(s: String) = {
    val acquire = IO(println(s"Acquiring $s")).map{_ => s}

    def release(s: String) = IO(println(s"Releasing $s"))

    Resource.make(acquire)(release)
  }

  val r = for {
    outer <- mkResource("outer")
    inner <- mkResource("inner")
  } yield (outer, inner)

  r.use { case (a, b) => IO(println(s"Using $a and $b")) }.unsafeRunSync
}
