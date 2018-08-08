package endpoint

import endpoint._
import endpoint.file.FileOps

import cats.effect.IO

import java.io.File

object Main extends App {

  val origin = new File("origin.txt")
  val destination = new File("destination.txt")

  import scala.concurrent.ExecutionContext.Implicits.global
  val program: IO[Unit] = for {
    fiber <- FileOps.copy(origin, destination).start
    copied <- fiber.join // Will never get there is the fiber is cancelled! In this example is the raiseError who 'breaks' the IO
    _ <- IO { println(s"Copied $copied bytes") }
  } yield ()

  program.unsafeRunSync()

}
