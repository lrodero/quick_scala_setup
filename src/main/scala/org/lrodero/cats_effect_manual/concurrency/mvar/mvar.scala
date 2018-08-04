package org.lrodero.cats_effect_manual.concurrency.mvar

import cats.effect.IO
import cats.effect.concurrent.MVar
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  def sum(l: List[Int], acc : MVar[IO, Int]): IO[Int] =
    l match {
      case h :: t => acc.take.flatMap { total =>
        acc.put(total + h).flatMap { _ =>
          sum(t, acc)
        }
      }
      case Nil => acc.take
    }

  val programSum = for {
    acc <- MVar.of[IO, Int](0)
    total <- sum((0 until 100).toList, acc)
  } yield total

  println(programSum.unsafeRunSync())


  final class Semaphore(mvar: MVar[IO, Unit]) {
    def acquire: IO[Unit] =
      mvar.take

    def release: IO[Unit] =
      mvar.put(())

    def greenLight[A](fa: IO[A]): IO[A] =
      acquire.bracket(_ => fa)(_ => release)
  }

  object Semaphore {
    def apply(): IO[Semaphore] =
      MVar[IO].empty[Unit].map(ref => new Semaphore(ref))
  }

  type Channel[A] = MVar[IO, Option[A]]

  def send(ch: Channel[Int], l: List[Int]): IO[Unit] =
    l match {
      case Nil => ch.put(None)
      case h :: t => ch.put(Option(h)) flatMap {_ => send(ch, t)}
    }

  def receive(ch: Channel[Int], acc: Long): IO[Long] = 
    ch.take.flatMap {
      case Some(i) => receive(ch, i + acc)
      case None => IO.pure(acc)
    }

  val program = for {
    ch <- MVar[IO].empty[Option[Int]]
    list = (0 until 100).toList
    _ <- send(ch, list)
    total <- receive(ch, 0)
  } yield total

  println(program.unsafeRunSync())

}

