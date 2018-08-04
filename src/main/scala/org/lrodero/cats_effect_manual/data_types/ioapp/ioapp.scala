package org.lrodero.cats_effect_manual.data_types.ioapp

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.syntax.all._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    args.headOption match {
      case Some(arg) => IO(println(s"Hello $arg")).as(ExitCode.Success)
      case None => IO(System.err.println("Usage Main name")).as(ExitCode(2))
    }

}

