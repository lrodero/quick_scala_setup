package endpoint

import cats.effect.IO

import java.io._
import java.nio.ByteBuffer

trait Encoding[A] {
  val length: Int
  def fromByteArray(arr: Array[Byte]) = fromByteArray(arr, 0)
  def fromByteArray(arr: Array[Byte], offset: Int): Either[Throwable, A] = 
    if(arr.length - offset < length) Left(new Error(s"Not enough bytes in array, we need $length"))
    else get(arr, offset)
  private def get(arr: Array[Byte], offset: Int): Either[Throwable, A]

  def toByteArray(a: A): Either[Throwable, Array[Byte]]

}

object Encoding {

  object instances {

    object JAVA_ENCODING {

      implicit object IntEnc extends Encoding[AnyVal] {
        val length = 
      }

    }

  }

}

// Let's assume is only files for the moment

abstract sealed class Closeable {
  val closeable: java.io.Closeable

  def close(): IO[Unit] = IO {
    closeable.close()
  }.recoverWith(()) // It swallows any potential error on close, yep :/
  
  def closeBecause(err: Throwable): IO[Unit] = close().raiseError(err)
}

final class WriteEndpoint(output: OutputStream) extends Closeable {

  val closeable = output

  private def writeFromByteArray(arr: Array[Byte]): IO[Unit] =
    IO { output.write(arr) }

  def write[A: Encoding](a): IO[Unit] = for {
    val enc = implicitly[Encoding[A]]
    
    val io = for {
      arr <- IO.fromEither(enc.toByteArray(a))
      _ <- writeFromByteArray(arr)
    } yield ()

    io.handleErrorWith { err =>
      closeBecause(err)
    }
  }

}

final class ReadEndpoint(input: InputStream) extends Closeable {

  val closeable = input

  private def readToByteArray(length: Int): IO[Array[Byte]] = for {
    arr <- IO.pure(new Array[Byte](length))
    numRead <- IO { input.read(arr) }
    _ <- if(numRead < length) IO.raiseError(new Error("")) // well, fuck
         else IO.unit // all good
  } yield arr


  def read[A: Encoding]: IO[A] = {
    val enc = implicitly[Encoding[A]]

    val io =  for {
      arr <- readToByteArray(enc.length)
      a <- IO.fromEither(enc.fromByteArray(arr))
    } yield a

    io.handleErrorWith { err =>
      closeBecause(err)
    }
  }

}

object Endpoint {

  def openToRead(file: String): IO[ReadEndpoint] = IO {
      val is = new BufferedInputStream(new FileInputStream(file)))
      new ReadEndpoint(is)
    }

  def openToWrite(file: String): IO[WriteEndpoint] = IO {
      val os = new BufferedOutputStream(new FileOutputStream(file)))
      new WriteEndpoint(os)
    }
}

object Main extends App {
}
