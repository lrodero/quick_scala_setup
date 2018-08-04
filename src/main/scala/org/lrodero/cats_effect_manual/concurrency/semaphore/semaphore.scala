package org.lrodero.cats_effect_manual.concurrency.semaphore

import cats.Parallel
import cats.effect.{Concurrent, IO, Timer}
import cats.effect.concurrent.Semaphore
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends App {
  class PreciousResource[F[_]](name: String, s: Semaphore[F])(implicit F: Concurrent[F], T: Timer[F]) {

    def use: F[Unit] =
      for {
        x <- s.available
        _ <- F.delay(println(s"$name >> Availability: $x"))
        _ <- s.acquire
        y <- s.available
        _ <- F.delay(println(s"$name >> Started | Availability: $y"))
        _ <- T.sleep(3.seconds)
        _ <- s.release
        z <- s.available
        _ <- F.delay(println(s"$name >> Done | Availability: $z"))
      } yield ()

  }

  implicit val par: Parallel[IO, IO] = Parallel[IO, IO.Par].asInstanceOf[Parallel[IO, IO]]

  val program: IO[Unit] =
    for {
      s  <- Semaphore[IO](1)
      r1 = new PreciousResource[IO]("R1", s)
      r2 = new PreciousResource[IO]("R2", s)
      r3 = new PreciousResource[IO]("R3", s)
      _  <- List(r1.use, r2.use, r3.use).parSequence.void
    } yield ()
}
