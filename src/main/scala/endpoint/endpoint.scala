package endpoint

import cats.effect.IO
import cats.effect.IO._
import cats.implicits._

import java.io._

/**
 * Wraps with IO instances read/writing actions on the provided streams. No action is taken
 * in case of error, e.g. the stream is not closed. Is the caller who better how to behave
 * when errors are triggered.
 */
trait Encoding[A] {
  def read(dis: DataInputStream): IO[A]
  def write(a: A, dos: DataOutputStream): IO[Unit]
}

object Encoding {

  object instances {

    object JAVA_ENCODING {

      implicit object EncInt extends Encoding[Int] {
        def read(dis: DataInputStream): IO[Int] = IO {
          dis.readInt()
        }
        def write(i: Int, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeInt(i)
        }
      }

      implicit object EncShort extends Encoding[Short] {
        def read(dis: DataInputStream): IO[Short] = IO {
          dis.readShort()
        }
        def write(s: Short, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeShort(s)
        }
      }

      implicit object EncLong extends Encoding[Long] {
        def read(dis: DataInputStream): IO[Long] = IO {
          dis.readLong()
        }
        def write(l: Long, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeLong(l)
        }
      }

      implicit object EncDouble extends Encoding[Double] {
        def read(dis: DataInputStream): IO[Double] = IO {
          dis.readDouble()
        }
        def write(d: Double, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeDouble(d)
        }
      }

      implicit object EncFloat extends Encoding[Float] {
        def read(dis: DataInputStream): IO[Float] = IO {
          dis.readFloat()
        }
        def write(f: Float, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeFloat(f)
        }
      }

      implicit object EncByte extends Encoding[Byte] {
        def read(dis: DataInputStream): IO[Byte] = IO {
          dis.readByte()
        }
        def write(b: Byte, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeByte(b)
        }
      }

      implicit object EncBoolean extends Encoding[Boolean] {
        def read(dis: DataInputStream): IO[Boolean] = IO {
          dis.readBoolean()
        }
        def write(b: Boolean, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeBoolean(b)
        }
      }

      implicit object EncChar extends Encoding[Char] {
        def read(dis: DataInputStream): IO[Char] = IO {
          dis.readChar()
        }
        def write(c: Char, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeChar(c)
        }
      }

      implicit object EncString extends Encoding[String] {
        def read(dis: DataInputStream): IO[String] = IO {
          dis.readUTF()
        }
        def write(s: String, dos: DataOutputStream): IO[Unit] = IO {
          dos.writeUTF(s)
        }
      }

    }

  }

}

abstract sealed class Closeable {
  val closeable: java.io.Closeable

  def close(): IO[Unit] = IO {
    closeable.close()
  }.handleErrorWith{_ => IO.unit} // It swallows any potential error on close, yep :/
  
  def closeWithErr[A](err: Throwable): IO[A] = close() *> IO.raiseError[A](err)
}

final class WriteEndpoint(output: OutputStream) extends Closeable {

  override val closeable = output

  private val dos = new DataOutputStream(output)

  def write[A: Encoding](a: A): IO[Unit] =
    implicitly[Encoding[A]]
      .write(a, dos)
      .handleErrorWith(closeWithErr[Unit](_))

  def writeByteArray(ba: Array[Byte]): IO[Unit] = IO {
      output.write(ba)
    }.handleErrorWith(closeWithErr[Unit](_))

  def writeByteArray(ba: Array[Byte], offset: Int, length: Int): IO[Unit] = IO {
      output.write(ba, offset, length)
    }.handleErrorWith(closeWithErr[Unit](_))
}

final class ReadEndpoint(input: InputStream) extends Closeable {

  override val closeable = input

  private val dis = new DataInputStream(input)

  def read[A: Encoding]: IO[A] =
    implicitly[Encoding[A]]
      .read(dis)
      .handleErrorWith(closeWithErr[A](_))

  def readByteArray(arr: Array[Byte]): IO[Int] = IO {
    input.read(arr)
  }.handleErrorWith(closeWithErr[Int](_))

  def readByteArray(arr: Array[Byte], offset: Int, length: Int): IO[Int] = IO {
    input.read(arr, offset, length)
  }.handleErrorWith(closeWithErr[Int](_))

}

object Endpoint {

  def openToRead(file: String): IO[ReadEndpoint] = IO {
      val is = new BufferedInputStream(new FileInputStream(file))
      new ReadEndpoint(is)
    }

  def openToWrite(file: String): IO[WriteEndpoint] = IO {
      val os = new BufferedOutputStream(new FileOutputStream(file))
      new WriteEndpoint(os)
    }

  def openToRead(file: File): IO[ReadEndpoint] = IO {
      val is = new BufferedInputStream(new FileInputStream(file))
      new ReadEndpoint(is)
    }

  def openToWrite(file: File): IO[WriteEndpoint] = IO {
      val os = new BufferedOutputStream(new FileOutputStream(file))
      new WriteEndpoint(os)
    }

  def copy(origin: ReadEndpoint, destination: WriteEndpoint): IO[Long] =
    copyInBatchesOf(origin, destination, 1024)


  def copyInBatchesOf(origin: ReadEndpoint, destination: WriteEndpoint, batchSize: Int) = {

    // Reads and writes in batches of 1024 bytes
    val batch = new Array[Byte](batchSize)

    def copyBatch(origin: ReadEndpoint, destination: WriteEndpoint, arr: Array[Byte]): IO[Int] = for {
      read <- origin.readByteArray(arr)
      _ <- if(read > -1) destination.writeByteArray(arr, 0, read)
           else IO.unit // End of read stream reached, nothing to write
    } yield read

    def copy(origin: ReadEndpoint, destination: WriteEndpoint, acc: Long): IO[Long] = for {
      copied <- copyBatch(origin, destination, batch)
      total <- if(copied > -1) copy(origin, destination, acc + copied)
           else IO.pure(acc)
    } yield total

    copy(origin, destination, 0L)
  }

  def copyFiles(origin: File, destination: File): IO[Long] = ???


}

object Main extends App {

  val origin = "origin.txt"
  val destination = "destination.txt"


  val program = (Endpoint.openToRead(origin), Endpoint.openToWrite(destination))
    .tupled
    .bracket{ case (in, out) => 
      Endpoint.copy(in, out).flatMap{copied => IO(println(s"Copied $copied bytes"))}
    }{ case (in, out) =>
      (in.close(), out.close()).tupled *> IO.unit
    }

  program.unsafeRunSync()

}
