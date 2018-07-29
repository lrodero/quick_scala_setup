package org.lrodero.fpformortalsbook.section_01

// import scalaz._, Scalaz._
// import simulacrum

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Terminal[C[_]] {
  def read: C[String]
  def write(c: String): C[Unit]
}

trait Execution[C[_]] {
  def doAndThen[A, B](c: C[A])(f: A => C[B]): C[B]
  def create[A](a: A): C[A]
}

object Execution {
  implicit class Ops[A, C[_]](c: C[A]) {
    def flatMap[B](f: A => C[B])(implicit e: Execution[C]): C[B] =
      e.doAndThen(c)(f)
    def map[B](f: A => B)(implicit e: Execution[C]): C[B] =
      e.doAndThen(c){f andThen e.create}
  }
}

object Terminal {
  type Now[X] = X
  object instances {
    object TerminalSync extends Terminal[Now] {
      def read: String = ???
      def write(s: String): Unit = ???
    }
    object TerminalAsync extends Terminal[Future] {
      def read: Future[String] = Future(TerminalSync.read)
      def write(s: String): Future[Unit] = Future(TerminalSync.write(s))
    }
  }
}

class IO[A](val interpret: () => A) {
  def map[B](f: A => B): IO[B] = IO(f(interpret()))
  def flatMap[B](f: A => IO[B]): IO[B] = f(interpret())
}
object IO {
  def apply[A](a: =>A): IO[A] = new IO(() => a)

  implicit val terminalIO: Terminal[IO] = new Terminal[IO] {
    def read: IO[String] = IO {io.StdIn.readLine}
    def write(s: String): IO[Unit] = IO {println(s)}
  }

  implicit val executionIO: Execution[IO] = new Execution[IO] {
    def doAndThen[A, B](c: IO[A])(f: A => IO[B]): IO[B] =
      c.flatMap(f)
    def create[A](a: A): IO[A] =
      IO[A](a)
  }

}

object Main extends App {

  def echo[C[_]](term: Terminal[C], exec: Execution[C]): C[String] =
    exec.doAndThen(term.read){str => 
      exec.doAndThen(term.write(str)) { _ => exec.create(str)
      }
    }

  import Execution._
  def echo2[C[_]](term: Terminal[C])(implicit e: Execution[C]): C[String] =
    term.read.flatMap(str => term.write(str).map(_ => str))

  def echo3[C[_]](term: Terminal[C])(implicit e: Execution[C]): C[String] =
    for {
      str <- term.read
      _   <- term.write(str)
    } yield str

  def echo4[C[_]](implicit t: Terminal[C], e: Execution[C]): C[String] = 
    for {
      str <- t.read
      _   <- t.write(str)
    } yield str

//  import IO._
//  val ec: IO[String] = echo4[IO] 

}
