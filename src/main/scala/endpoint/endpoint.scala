package endpoint
import cats.effect.IO
import cats.effect.IO._
import cats.implicits._

import endpoint.encoding.Encoding

import java.io._

/**
 * Wraps <code>close()</code> operation of [[java.io.Closeable]] inside an
 * IO instance. Also it adds the [[closeWithErr]] function that allows to
 * add an exception to that enclosing IO.
 */
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

  def available: IO[Int] = IO {
    dis.available()
  }.handleErrorWith(closeWithErr[Int](_))

  def mark(readLimit: Int): IO[Unit] = IO {
    dis.mark(readLimit)
  }.handleErrorWith(closeWithErr[Unit](_))

  def markSupported: IO[Boolean] = IO {
    dis.markSupported()
  }.handleErrorWith(closeWithErr[Boolean](_))

  def read[A: Encoding]: IO[A] =
    implicitly[Encoding[A]]
      .read(dis)
      .handleErrorWith(closeWithErr[A](_))

  def readByteArray(arr: Array[Byte]): IO[Int] = IO {
    dis.read(arr)
  }.handleErrorWith(closeWithErr[Int](_))

  def readByteArray(arr: Array[Byte], offset: Int, length: Int): IO[Int] = IO {
    dis.read(arr, offset, length)
  }.handleErrorWith(closeWithErr[Int](_))

  def reset: IO[Unit] = IO {
    dis.reset()
  }.handleErrorWith(closeWithErr[Unit](_))

  def skipBytes(n: Int): IO[Int] = IO {
    dis.skipBytes(n)
  }.handleErrorWith(closeWithErr[Int](_))

  def skip(n: Long): IO[Long] = IO {
    dis.skip(n)
  }.handleErrorWith(closeWithErr[Long](_))

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
    def transferLoop(origin: ReadEndpoint, destination: WriteEndpoint, arr: Array[Byte], acc: Long): IO[Long] = for {
      _ <- IO.cancelBoundary
      amount <- transfer(origin, destination, arr)
      total <- if(amount > -1) transferLoop(origin, destination, arr, acc + amount)
               else IO.pure(acc)
    } yield total

    for {
      arr <- IO.pure(new Array[Byte](batchSize))
      acc <- transferLoop(origin, destination, arr, 0L)
    } yield acc
  }

  /**Copy <code>length</code> bytes from <code>origin</code> to <code>destination</code> by calling
   * to [[copyInBatchesOf]] with batch size [[DEFAULT_BATCH_SIZE]].
   */
  def copy(origin: ReadEndpoint, destination: WriteEndpoint, length: Long): IO[Long] =
    copyInBatchesOf(origin, destination, length, DEFAULT_BATCH_SIZE)

  /**Copy contents of <code>origin</code> to <code>destination</code>, until <code>length</code> bytes
   * have been copied or the end of the <code>origin</code> [[java.io.Inputstream]] is reached or
   * the IO instance returned is cancelled. Cancellation will <em>NOT</em> close both <code>origin</code>
   * and <code> destination</code>. Recall that:
   * <ul>
   *   <li> Cancelling a copy IO operation does not set the origin or destination into their
   *        original state. So for example when writing to a file, data written before the
   *        cancellation will be available there.</li>
   *   <li> If some error is caught in one of the endpoints (<em>e.g.</em> an <code>IOException</code>
   *        is caught) only the endpoint affected is closed.</li>
   * </ul>
   */
  def copyInBatchesOf(origin: ReadEndpoint, destination: WriteEndpoint, length: Long, batchSize: Int): IO[Long] = {

    def transferLoop(origin: ReadEndpoint, destination: WriteEndpoint, arr: Array[Byte], acc: Long): IO[Long] =
      if(acc >= length)
        IO.pure(acc)
      else
        for {
          _ <- IO.cancelBoundary
          remaining = acc - length
          amount <- if(remaining >= batchSize) transfer(origin, destination, arr)
                    else transfer(origin, destination, arr, 0, remaining.toInt)
          total <- if(amount > -1) transferLoop(origin, destination, arr, acc + amount)
                   else IO.pure(acc)
          } yield total

   for {
     arr <- IO.pure(new Array[Byte](batchSize))
     acc <- transferLoop(origin, destination, arr, 0L)
   } yield acc
  }

  private def transfer(origin: ReadEndpoint, destination: WriteEndpoint, arr: Array[Byte]): IO[Int] = 
    transfer(origin, destination, arr, 0, arr.size)

  private def transfer(origin: ReadEndpoint, destination: WriteEndpoint, arr: Array[Byte], offset: Int, length: Int): IO[Int] = for {
    amount <- origin.readByteArray(arr, offset, length)
    _ <- if(amount > -1) destination.writeByteArray(arr, offset, amount)
         else IO.unit // End of read stream reached, nothing to write
  } yield amount
}

// TODO:
//  - TCP tests
//  - Stream data when read (Monix observable?), write data from stream
//  - The encoding JAVA_BINARY should be improved with shapeless for automatic derivation
//    of Encoding instances for ADTs

