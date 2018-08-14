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
import cats.effect.concurrent.{MVar, Ref}
import cats.implicits._

import java.io._
import java.net.{ServerSocket, Socket}


// TODO: keep set of open clients, to close them all if serve must be closed
object MainTCP extends IOApp {

  // Just and 'echo' server. It will quit when it gets an empty line. If it gets 'CLOSE' then it will stop the whole server.
  def attendNewClient(clientSocket: Socket, stopServerFlag: MVar[IO, Unit]): IO[Unit] = {

    def loop(reader: BufferedReader, writer: BufferedWriter): IO[Unit] = for {
      _    <- IO.cancelBoundary
      line <- reader.readLineIO
      _    <- line match {
                case "CLOSE" => stopServerFlag.put(())
                case ""      => IO.unit
                case _       => writer.writeIO(line) *> writer.newLineIO *> writer.flushIO *> loop(reader, writer)
              }
    } yield ()

    def close(reader: BufferedReader, writer: BufferedWriter): IO[Unit] = 
      IO{ println("Closing client reader & writer")  }*> (reader.closeIO, writer.closeIO).tupled.map(_ => ()).handleErrorWith(_ => IO.unit)

    (SocketIO.getReader(clientSocket), SocketIO.getWriter(clientSocket))
      .tupled
      .bracketCase {
        case (reader, writer) => loop(reader, writer).guarantee(close(reader, writer))
      } { 
        case ((reader, writer), Completed)  => IO{ println("Finished service to client normally") }
        case ((reader, writer), Canceled)   => IO{ println(s"Finished service to client because cancellation") }
        case ((reader, writer), Error(err)) => IO{ println(s"Finished service to client due to error: '${err.getMessage}'") }
      }  
  } 

  def serve(serverSocket: ServerSocket, stopServerFlag: MVar[IO, Unit]): IO[Unit] = {

    def closeClientSocket(clientSocket: Socket): IO[Unit] =
      IO{ println("Closing client socket") } *> clientSocket.closeIO.handleErrorWith(_ => IO.unit)

    def acceptNewConnections(activeClients: Ref[IO, Set[Fiber[IO, Unit]]]): IO[Unit] = for {
      _             <- IO.cancelBoundary
      clientSocketE <- serverSocket.acceptIO.attempt
      _             <- clientSocketE match {
                         case Right(clientSocket) => for {
                            clientFiber <- attendNewClient(clientSocket, stopServerFlag).guarantee(closeClientSocket(clientSocket)).start 
                            _ <- activeClients.update(_ + clientFiber) 
                            _ <- (clientFiber.join *> activeClients.update(_ - clientFiber)).start
                            _ <- acceptNewConnections(activeClients)
                           } yield ()
                         case Left(e) => stopServerFlag.isEmpty >>= (isEmpty =>
                                           if(!isEmpty) IO.unit // Server socket was closed, which triggered an exception on 'accept' that we ignore
                                           else IO.raiseError(new java.lang.Error("Unexpected error", e))
                                         )
                       }
    } yield ()

    for {
      activeClients <- Ref.of[IO, Set[Fiber[IO, Unit]]](Set.empty[Fiber[IO, Unit]])
      _             <- acceptNewConnections(activeClients).guarantee{ for {
                         clients <- activeClients.get
                         _       <- IO { println(s"No more client connections accepted, closing ${clients.size} still active clients") }
                         _       <- IO { clients.foreach(_.cancel) }
                         } yield ()
                       }
    } yield ()

  }

  def server(serverSocket: ServerSocket): IO[ExitCode] = for {
    stopServerFlag      <- MVar[IO].empty[Unit]
    serverFiber         <- serve(serverSocket, stopServerFlag).start
    _                   <- stopServerFlag.read
    _                   <- serverFiber.cancel
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] = {

    def closeServerSocket(serverSocket: ServerSocket): IO[Unit] =
      IO{ println("Closing server socket") } *> serverSocket.closeIO.handleErrorWith(_ => IO.unit)

    ServerSocketIO.serverSocket(Option(5002), None, None)
      .bracketCase {
        case serverSocket => server(serverSocket).guarantee(closeServerSocket(serverSocket))
      } {
        case (_, Error(err)) => IO{ println(s"Server was stopped due to an error: ${err.getMessage}") }
        case (_, Canceled)   => IO{ println("Server execution was cancelled") }
        case (_, Completed)  => IO{ println("Server was successfully run") }
      }
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
