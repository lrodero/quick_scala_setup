package org.lrodero.cats_effect_manual.data_types.fiber

import cats.effect.{Fiber, IO}

import cats.syntax.apply._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  val io = IO{ println("Hello") }
  val fiber: IO[Fiber[IO, Unit]] = io.start

  def launchMissiles = IO.raiseError(new Error("boom!"))
  def runToBunker = IO { println("Running to bunker!") }

  for {
    fiber <- launchMissiles.start
    _ <- runToBunker.handleErrorWith { err =>
      fiber.cancel *> IO.raiseError(err)
    }
    aftermath <- fiber.join
  } yield aftermath

}

