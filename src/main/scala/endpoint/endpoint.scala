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

  def close: IO[Unit] = IO {
    closeable.close()
  }.handleErrorWith{_ => IO.unit} // It swallows any potential error on close, yep :/
  
  def closeWithErr[A](err: Throwable): IO[A] = close *> IO.raiseError[A](err)
}


/** Wraps a [[java.io.OutputStream]] so write operations are [[cats.effect.IO]]
 *  instances. If some error is detected in a write operation, the stream is
 *  closed automatically, but the error is kept by the IO instance.
 */
final class WriteEndpoint(output: OutputStream) extends Closeable {

  private val dos = new DataOutputStream(output)

  override val closeable = dos

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

  def flush: IO[Unit] = IO {
    dos.flush()
  }.handleErrorWith(closeWithErr[Unit](_))

}

/** Wraps a [[java.io.InputStream]] so read operations are [[cats.effect.IO]]
 *  instances. If some error is detected in a read operation, the stream is
 *  closed automatically, but the error is kept by the IO instance.
 */
final class ReadEndpoint(input: InputStream) extends Closeable {

  private val dis = new DataInputStream(input)

  override val closeable = dis

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

/** Wraps a [[java.io.Writer]] to write char streams,so write operations
 *  are [[cats.effect.IO]] instances. If some error is detected in a
 *  write operation, the writer is closed automatically, but the error
 *  is kept by the IO instance.
 */
final class WriteTxtEndpoint(writer: Writer) extends Closeable {

  private val bw = new BufferedWriter(writer)

  override val closeable = bw

  def newLine: IO[Unit] = IO {
    bw.newLine()
  }.handleErrorWith(closeWithErr[Unit](_))

  def write(cbuf: Array[Char], offset: Int, length: Int): IO[Unit] = IO {
    bw.write(cbuf, offset, length)
  }.handleErrorWith(closeWithErr[Unit](_))

  def write(c: Int): IO[Unit] = IO {
    bw.write(c)
  }.handleErrorWith(closeWithErr[Unit](_))

  def write(s: String, offset: Int, length: Int): IO[Unit] = IO {
    bw.write(s, offset, length)
  }.handleErrorWith(closeWithErr[Unit](_))

  def write(s: String): IO[Unit] = write(s, 0, s.size)

  def flush: IO[Unit] = IO {
    bw.flush()
  }.handleErrorWith(closeWithErr[Unit](_))

}

/** Wraps a [[java.io.Reader]] to read char streams,so read operations
 *  are [[cats.effect.IO]] instances. If some error is detected in a
 *  read operation, the reader is closed automatically, but the error
 *  is kept by the IO instance.
 */
final class ReadTxtEndpoint(reader: Reader) extends Closeable {

    private val br = new BufferedReader(reader)

    override val closeable = br

    def mark(readAheadLimit: Int): IO[Unit] = IO {
      br.mark(readAheadLimit)
    }.handleErrorWith(closeWithErr[Unit](_))

    def markSupported: IO[Boolean] = IO {
      br.markSupported()
    }.handleErrorWith(closeWithErr[Boolean](_))

    def read: IO[Int] = IO {
      br.read()
    }.handleErrorWith(closeWithErr[Int](_))

    def read(cbuf: Array[Char], offset: Int, length: Int): IO[Int] = IO {
      br.read(cbuf, offset, length)
    }.handleErrorWith(closeWithErr[Int](_))

    def readLine: IO[String] = IO {
      br.readLine()
    }.handleErrorWith(closeWithErr[String](_))

    def ready: IO[Boolean] = IO {
      br.ready()
    }.handleErrorWith(closeWithErr[Boolean](_))

    def reset: IO[Unit] = IO {
      br.reset()
    }.handleErrorWith(closeWithErr[Unit](_))

    def skip(n: Long): IO[Long] = IO {
      br.skip(n)
    }.handleErrorWith(closeWithErr[Long](_))
}

object Endpoint {

  final var DEFAULT_BATCH_SIZE = 10 * 1024

  /**Copy contents of <code>origin</code> to <code>destination</code> by calling to
   * [[copyInBatchesOf]] with batch size [[DEFAULT_BATCH_SIZE]].
   */
  def copy(origin: ReadEndpoint, destination: WriteEndpoint): IO[Long] =
    copyInBatchesOf(origin, destination, batchSize = DEFAULT_BATCH_SIZE)

  /**Copy contents of <code>origin</code> to <code>destination</code>, until the end of the
   * <code>origin</code> [[java.io.Inputstream]] is reached or the IO instance returned is
   * cancelled. Cancellation will <em>NOT</em> close both <code>origin</code> and <code>
   * destination</code>. Recall that:
   * <ul>
   *   <li> Cancelling a copy IO operation does not set the origin or destination into their
   *        original state. So for example when writing to a file, data written before the
   *        cancellation will be available there.</li>
   *   <li> If some error is caught in one of the endpoints (<em>e.g.</em> an <code>IOException</code>
   *        is caught) only the endpoint affected is closed.</li>
   * </ul>
   */
  def copyInBatchesOf(origin: ReadEndpoint, destination: WriteEndpoint, batchSize: Int): IO[Long] = {

    // Reads and writes in 'batches', i.e. arrays of bytes. We reuse always the same array.
    val batch = new Array[Byte](batchSize)

    def copyBatch(origin: ReadEndpoint, destination: WriteEndpoint, arr: Array[Byte]): IO[Int] = for {
      amount <- origin.readByteArray(arr)
      _ <- if(amount > -1) destination.writeByteArray(arr, 0, amount)
           else IO.unit // End of read stream reached, nothing to write
    } yield amount

    def copy(origin: ReadEndpoint, destination: WriteEndpoint, acc: Long): IO[Long] = for {
      _ <- IO.cancelBoundary
      amount <- copyBatch(origin, destination, batch)
      total <- if(amount > -1) copy(origin, destination, acc + amount)
               else IO.pure(acc)
    } yield total

    copy(origin, destination, 0L)
  }

}

// TODO:
//  - TCP
//  - Stream data when read (Monix observable?), write data from stream

object Main extends App {

  val origin = new File("origin.txt")
  val destination = new File("destination.txt")

  import scala.concurrent.ExecutionContext.Implicits.global
  val program: IO[Unit] = for {
    fiber <- endpoint.file.FileOps.copy(origin, destination).start
    copied <- fiber.join // Will never get there is the fiber is cancelled! In this example is the raiseError who 'breaks' the IO
    _ <- IO { println(s"Copied $copied bytes") }
  } yield ()

  program.unsafeRunSync()

}
