package scalawithcats.chapter03_functors

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import cats.Eq
import cats.instances.double._ // for Eq[Double]
import cats.instances.function._ // for Functor[Function1]
import cats.syntax.eq._ // for '===' and '=!='
import cats.syntax.functor._ // for 'map'

object Main extends App {

  val f =
    Future(123)
      .map(_ + 1)
      .map(_ * 2)
      .map(_  + "!")

  println(Await.result(f, 1 second))
   
  // Single argument functions are also functors!
  println("\n--- Functor[Function1] ---")
  val func1: Int => Double = _.toDouble
  val func2: Double => Double = _ * 2

  println((func1 map func2)(1))
  println((func1 andThen func2)(1))
  println( (func1 map func2)(100) === (func1 andThen func2)(100) )

}

