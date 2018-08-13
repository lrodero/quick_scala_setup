package endpoint

import endpoint._
import endpoint.Endpoint._
import endpoint.file.FileIO
import endpoint.socket.ServerSocketIO
import endpoint.socket.ServerSocketIO._
import endpoint.socket.SocketIO
import endpoint.socket.SocketIO._

import cats.Eval
import cats.effect.{Fiber, IO, IOApp}
import cats.effect.ExitCode
import cats.effect.ExitCase._
import cats.effect.concurrent.MVar
import cats.implicits._

import java.io._
import java.net.{ServerSocket, Socket}


// TODO: keep set of open clients, to close them all if serve must be closed
object MainTCP extends IOApp {

  def serve(port: Int, stopServerFlag: MVar[IO, Unit]): IO[Unit] = {

    def acceptNewConnections(serverSocket: ServerSocket): IO[Unit] = for {
      _             <- IO.cancelBoundary
      clientSocketE <- serverSocket.acceptIO.attempt
      _             <- clientSocketE match {
                         case Right(clientSocket) => attendNewClient(clientSocket, stopServerFlag).start *> acceptNewConnections(serverSocket)
                         case Left(e) => stopServerFlag.isEmpty >>= (isEmpty =>
                                              if(!isEmpty) IO.unit // Server socket was intended to be closed, which triggered this exception. We can safely ignore it.
                                              else IO.raiseError(new java.lang.Error("Unexpected error", e))
                                         )
                       }
    } yield ()

    def close(serverSocket: ServerSocket): IO[Unit] = IO{serverSocket.close()}.handleErrorWith(_ => IO.unit)

    ServerSocketIO.serverSocket(Option(port), None, None)
      .bracketCase {
        serverSocket => acceptNewConnections(serverSocket).guarantee{ IO{ println("Closing server socket") } *> close(serverSocket) }
      } {
        case (_, Completed)  => IO{println("Finished server socket normally")}
        case (_, Canceled)   => IO{println("Finished server socket because cancellation")}
        case (_, Error(err)) => IO{println(s"Finished server socket due to error: '${err.getMessage}'")}
      }

  }

  // Just and 'echo' server. It will quit when it gets an empty line. If it gets 'CLOSE' then it will stop the whole server.
  def attendNewClient(clientSocket: Socket, stopServerFlag: MVar[IO, Unit]): IO[Unit] = {

    def loop(reader: BufferedReader, writer: BufferedWriter): IO[Unit] = for {
      line <- reader.readLineIO
      _    <- line match {
                case "CLOSE" => stopServerFlag.put(())
                case ""      => IO.unit
                case _       => writer.writeIO(line) *> writer.newLineIO *> writer.flushIO *> loop(reader, writer)
              }
    } yield ()

    def close(reader: BufferedReader, writer: BufferedWriter): IO[Unit] = 
      (reader.closeIO, writer.closeIO, clientSocket.closeIO).tupled.map(_ => ()).handleErrorWith(_ => IO.unit)

    (SocketIO.getReader(clientSocket), SocketIO.getWriter(clientSocket))
      .tupled
      .bracketCase {
        case (reader, writer) => loop(reader, writer).guarantee{ IO{ println("Closing client socket") } *> close(reader, writer) }
      } { 
        case (_, Completed)  => IO {println("Finished service to client normally")}
        case (_, Canceled)   => IO {println(s"Finished service to client because cancellation")}
        case (_, Error(err)) => IO {println(s"Finished service to client due to error: '${err.getMessage}'")}
      }  
  } 

  val server: IO[ExitCode] = for {
    stopServerFlag <- MVar[IO].empty[Unit]
    serverFiber    <- serve(5001, stopServerFlag).start
    _              <- stopServerFlag.read
    _              <- serverFiber.cancel
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] =
    server.guaranteeCase {
      case Error(err) => IO{ println(s"Server was stopped due to an error: ${err.getMessage}") }
      case Canceled   => IO{ println("Server execution was cancelled") }
      case Completed  => IO{ println("Server was successfully run") }
    }
}

object MainFile extends App {
  // File test //
  val origin = new File("origin.txt")
  val destination = new File("destination.txt")

  import scala.concurrent.ExecutionContext.Implicits.global
  val program: IO[Unit] = for {
    fiber <- FileIO.copy(origin, destination).start
    copied <- fiber.join // Will never get there is the fiber is cancelled! In this example is the raiseError who 'breaks' the IO
    _ <- IO { println(s"Copied $copied bytes") }
  } yield ()

  program.unsafeRunSync()

}
