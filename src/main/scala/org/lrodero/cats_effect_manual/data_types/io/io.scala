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
  def fibStackSafe(n: Int, a: Long = 0, b: Long = 1): IO[Long] = IO.suspend {
    if (n > 1) fib(n-1, b, a+b)
    else IO.pure(b)
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

  def sleep(d: FiniteDuration)(implicit sc: ScheduledExecutorService): IO[Unit] = IO.cancelable { cb =>
    val r = new Runnable{ def run() = cb(Right(())) }
    val f = sc.schedule(r, d.length, d.unit)
    IO(f.cancel(false))
  }

  // Concurrent start+cancel

  val launchMissiles = IO.raiseError{new Exception("boom!")}
  val runToBunker = IO{println("To the bunker!")}
  
  import scala.concurrent.ExecutionContext.Implicits.global
  import cats.effect.syntax.all._
  import cats.syntax.apply._
  val conc = for {
    fiber <- launchMissiles.start
    _ <- runToBunker.handleErrorWith { error =>
      fiber.cancel *> IO.raiseError(error)
    }
    aftermath <- fiber.join
  } yield {
    aftermath
  }

  //println(conc.unsafeRunSync()) <-- Won't catch the exception raised!
  conc.runAsync {
    case Left(e) => IO { println(s"ups: ${e.getMessage}") }
    case Right(_) => IO { println("All was good") }
  }.unsafeRunSync()

  def delayedGreeting = IO.sleep(2.seconds) *> IO(println("hi!"))

  val cancelDelayedGreeting = delayedGreeting.unsafeRunCancelable{
    case Left(e) => println(s"Done: ${e.getMessage}")
    case Right(r) => println(s"Done: $r")
  }

  cancelDelayedGreeting() // As we are cancelling, no msg will be shown

  val delayedGreetingPureResult = delayedGreeting.runCancelable { r =>
    IO { println(s"Done: $r") }
  }

  import cats.syntax.flatMap._
  val cancelDelayedGreetingSafe = delayedGreetingPureResult.flatten

  cancelDelayedGreetingSafe.unsafeRunSync() // As we are cancelling, no msg will be shown

  // cancelBoundary, to make long IOs cancelable at intermediate steps
  def fibWithCancelBoundary(n: Int, a: Long = 0, b: Long = 1): IO[Long] = IO.suspend {
    if (n > 0) {
      val nextFib = fibWithCancelBoundary(n - 1, b, a + b)
      if(n % 100 != 0) nextFib
      else IO.cancelBoundary *> nextFib
    } else {
      IO.pure(b)
    }
  }

  println(s"Fib with cancel boundary: ${fibWithCancelBoundary(5).unsafeRunSync()}")


  // bracket...
  import java.io.{BufferedReader, File, FileReader}
  def readFirstLine(file: File): IO[String] =
    IO{println("Obtaining resource");new BufferedReader(new FileReader(file))}.bracket{ br =>
      println("In USAGE action of bracked")
      //throw new Error("Non fatal")
      IO(br.readLine())
    } { br =>
      println("In RELEASE action of bracked")
      IO(br.close())
    }
  //readFirstLine(new File("thisfiledoesnotexist")).unsafeRunSync()
  readFirstLine(new File("README.md")).unsafeRunSync()

  // Conversions

  val ioFromFuture = IO.fromFuture(IO {
    Future(println(s"I come from the future"))
  })

  ioFromFuture.unsafeRunSync()

  import cats.effect.Timer
  def retryWithBackoff[A](ioa: IO[A], initialDelay: FiniteDuration, maxRetries: Int)(implicit timer: Timer[IO]): IO[A] = {
    if(maxRetries <= 0) IO.raiseError[A](new Throwable("Max number of tries reached"))
    else ioa.attempt.flatMap { 
      case Right(a) => IO.pure(a)
      case Left(err) => IO.sleep(initialDelay) *> retryWithBackoff(ioa, initialDelay * 2, maxRetries - 1)
    }
  }



}
