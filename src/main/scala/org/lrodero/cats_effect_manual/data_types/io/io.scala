package org.lrodero.cats_effect_manual.data_types.io

/** Following doc and examples at 
 *  https://typelevel.org/cats-effect/datatypes/io.html
 */

import cats.effect.IO

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicBoolean
import java.io.BufferedReader

import scala.concurrent._
import scala.concurrent.duration._
import scala.util._
import scala.util.control.NonFatal

object Main extends App {

  val ioa = IO[Unit]{ println("hello!") }

  val program = for {
    _ <- ioa
    _ <- ioa
  } yield ()

  program.unsafeRunSync()

  def fib(n: Int, a: Long = 0, b: Long = 1): IO[Long] = 
    IO(a + b).flatMap { b2 =>
      if (n > 1) fib(n-1, b, b2)
      else IO.pure(b2)
    }

  println(fib(5).unsafeRunSync())

  val pureEx = IO.pure(25).flatMap(i => IO{println(s"Number is $i")})
  pureEx.unsafeRunSync()

  // Use IO.unit for 'emtpy' IOs! It's more efficient and clean.
  
  def putStrLn(s: String) = IO{ println(s) }
  val readLn = IO{ scala.io.StdIn.readLine }

  val echoName = for {
    _ <- putStrLn("What's your name?")
    name <- readLn
    _ <- putStrLn(s"Hello $name!")
  } yield ()

  //echoName.unsafeRunSync()


  /* *** ASYNC *** */

  def fromFutureToAsyncIO[A](f: Future[A])(implicit ec: ExecutionContext): IO[A] = IO.async { cb =>
    f.onComplete { 
      case Success(a) => cb(Right(a))
      case Failure(e) => cb(Left(e))
    }
  }

  // Cancelable processes
  def delayedTick(d: FiniteDuration)(implicit sc: ScheduledExecutorService): IO[Unit] = IO.cancelable { cb =>
    val r = new Runnable{ def run() = cb(Right(()))}
    val f = sc.schedule(r, d.length, d.unit)

    IO(f.cancel(false)) // <- whis will be called when cancelled
  }

  // IO.suspend
  def fibStackSafe(n: Int, a: Long, b: Long): IO[Long] = IO.suspend {
    if (n > 1) fib(n-1, b, a+b)
    else IO.pure(a)
  }

  def readLine(in: BufferedReader)(implicit ec: ExecutionContext): IO[String] = IO.cancelable[String] { cb =>

    val isActive = new AtomicBoolean(true)
    ec.execute{ () =>
      if(isActive.getAndSet(false)) {
        try cb(Right(in.readLine()))
        catch {case NonFatal(e) => cb(Left(e))}
      }
    }

    IO {
      if(isActive.getAndSet(false)) {
        in.close()
      }
    }

  }

  // Concurrent start+cancel

  
  

}
