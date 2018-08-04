package org.lrodero.cats_effect_manual.concurrency.deferred

import cats.Parallel
import cats.effect.IO
import cats.effect.concurrent.Deferred
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
object Main extends App {

  implicit val par: Parallel[IO, IO] = Parallel[IO, IO.Par].asInstanceOf[Parallel[IO, IO]]

  def start(d: Deferred[IO, Int]): IO[Unit] = {

    def attempt(i: Int) = d.complete(i).attempt.void

    List(
      IO.race(attempt(1), attempt(2)),
      d.get.flatMap(i => IO{println(s"Got $i")})
    ).parSequence.void

  }

  val program = for {
    d <- Deferred[IO, Int]
    _ <- start(d)
  } yield ()

  program.unsafeRunSync()

}

