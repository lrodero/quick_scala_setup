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

    for {
      serverSocket <- ServerSocketIO.serverSocket(Option(port), None, None)
      _            <- acceptNewConnections(serverSocket).guarantee{IO{println("Closing server socket")} *> IO{serverSocket.close()}.handleErrorWith(_ => IO.unit)}
    } yield()

  }

  // Just and 'echo' server. It will quit when it gets an empty line
  def attendNewClient(clientSocket: Socket, stopServerFlag: MVar[IO, Unit]): IO[Unit] = {
    
    val readerIO = IO{ println(s"Creating reader for socket, is connected to remote port ${clientSocket.getPort}") } *> SocketIO.getReader(clientSocket)
    val writerIO = IO{ println(s"Creating writer for socket, is connected to remote port ${clientSocket.getPort}") } *> SocketIO.getWriter(clientSocket)

    def loop(reader: BufferedReader, writer: BufferedWriter): IO[Unit] = for {
      line <- reader.readLineIO
      _ <- if(line == "CLOSE") stopServerFlag.put(())
           else if(line == "") IO.unit
           else writer.writeIO(line) *> writer.newLineIO *> writer.flushIO *> loop(reader, writer)
    } yield ()

    (readerIO, writerIO)
      .tupled
      .bracketCase {
        case (reader, writer) => loop(reader, writer)
      } { 
        case ((reader, writer), Completed) =>
          IO {println("Closing client socket normally")} *> (reader.closeIO, writer.closeIO, clientSocket.closeIO).tupled.map(_ => ())
        case ((reader, writer), Canceled) =>
          IO {println(s"Closing client socket because cancellation")} *> (reader.closeIO, writer.closeIO, clientSocket.closeIO).tupled.map(_ => ())
        case ((reader, writer), Error(err)) =>
          IO {println(s"Closing client socket abnormally, ${err.getMessage}")} *> (reader.closeIO, writer.closeIO, clientSocket.closeIO).tupled.map(_ => ())
      }  
  } 

  override def run(args: List[String]): IO[ExitCode] = for {
    stopServerFlag <- MVar[IO].empty[Unit]
    serverFiber    <- serve(5001, stopServerFlag).start
    _              <- stopServerFlag.read
    _              <- serverFiber.cancel
  } yield ExitCode.Success

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
