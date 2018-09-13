package tutorial

import cats.effect._
import cats.effect.concurrent.{Deferred, MVar}
import cats.implicits._

object TestMVar extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    val test1 = for {
      greenLight <- Deferred[IO, Unit]
      _ <- (IO{println("Started fiber 1")} *> greenLight.get *> IO{println("Finished fiber 1")}).start
      _ <- (IO{println("Started fiber 2")} *> greenLight.get *> IO{println("Finished fiber 2")}).start
      _ <- (IO{println("Started fiber 3")} *> greenLight.get *> IO{println("Finished fiber 3")}).start
      _ <- IO{println("Please press <enter>")} *> IO{io.StdIn.readLine()} *> IO{println("<enter> pressed")} *> greenLight.complete(()) *> IO.shift
      greenLight2 <- Deferred[IO, Unit]
      _ <- (IO{println("Started fiber 4")} *> greenLight2.get *> IO{println("Finished fiber 4")}).start
      _ <- IO{println("Please press <enter> again ")} *> IO{io.StdIn.readLine()} *> IO{println("<enter> pressed")} *> greenLight2.complete(()) *> IO.shift
    } yield ()

    val test2 = for {
      greenLight <- MVar[IO].empty[Unit]
      greenLight2 <- MVar[IO].empty[Unit]
      fiberA <- IO.unit.bracket{_ => IO{println("Started fiber A")} *> greenLight.read *> IO{println("Finished fiber A")}}{_ => IO{println("Cancelled fiber A")}}.start
      fiberB <- IO.unit.bracket{_ => IO{println("Started fiber B")} *> greenLight.read *> IO{println("Finished fiber B")}}{_ => IO{println("Cancelled fiber B")}}.start
      fiberC <- IO.unit.bracket{_ => IO{println("Started fiber C")} *> greenLight.read *> IO{println("Finished fiber C")}}{_ => IO{println("Cancelled fiber C")}}.start
      _ <- (greenLight2.read *> fiberA.cancel *> fiberB.cancel *> fiberC.cancel).start
      _ <- IO{println("Please press <enter>")} *> IO{io.StdIn.readLine()} *> IO{println("<enter> pressed")} *> greenLight2.put(()) *> IO.shift
    } yield ()

    for {
      _ <- IO{println("Starting test1")}
      _ <- test1
      _ <- IO{println("Starting test2")}
      _ <- test2
      _ <- IO{println("All tests done")}
    } yield ExitCode.Success

  }

}
