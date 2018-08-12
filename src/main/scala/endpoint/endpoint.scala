package endpoint
import cats.effect.IO
import cats.effect.IO._
import cats.implicits._

import endpoint.encoding.Encoding

import java.io._
import java.util.stream.Stream

object Endpoint {

  implicit class CloseableIO(closeable: java.io.Closeable) {

    def closeIO: IO[Unit] = IO {
      closeable.close()
    }

    def closeWithErrIO[A](err: Throwable): IO[A] = closeIO *> IO.raiseError[A](err)
  }


  /** Wraps a [[java.io.OutputStream]] so write operations are [[cats.effect.IO]]
   *  instances. If some error is detected in a write operation, the stream is
   *  closed automatically, but the error is kept by the IO instance.
   */
  implicit class OutputStreamIO(output: OutputStream) {

    def writeEncodedIO[A: Encoding](a: A): IO[Unit] =
      implicitly[Encoding[A]]
        .write(a, output)

        def writeIO(ba: Array[Byte]): IO[Unit] = IO {
          output.write(ba)
        }

        def writeIO(ba: Array[Byte], offset: Int, length: Int): IO[Unit] = IO {
          output.write(ba, offset, length)
        }

        def flushIO: IO[Unit] = IO {
          output.flush()
        }

  }

  implicit class InputStreamIO(input: InputStream) {

    def availableIO: IO[Int] = IO {
      input.available()
    }

    def markIO(readLimit: Int): IO[Unit] = IO {
      input.mark(readLimit)
    }

    def markSupportedIO: IO[Boolean] = IO {
      input.markSupported()
    }

    def readEncodedIO[A: Encoding]: IO[A] =
      implicitly[Encoding[A]]
        .read(input)

        def readIO(): IO[Int] = IO {
          input.read()
        }

        def readIO(arr: Array[Byte]): IO[Int] = IO {
          input.read(arr)
        }

        def readIO(arr: Array[Byte], offset: Int, length: Int): IO[Int] = IO {
          input.read(arr, offset, length)
        }

        def resetIO: IO[Unit] = IO {
          input.reset()
        }

        def skipIO(n: Long): IO[Long] = IO {
          input.skip(n)
        }

  }

  /** Wraps a [[java.io.Writer]] to write char streams,so write operations
   *  are [[cats.effect.IO]] instances. If some error is detected in a
   *  write operation, the writer is closed automatically, but the error
   *  is kept by the IO instance.
   */
  implicit class WriterIO(writer: Writer) {

    def appendIO(c: Char): IO[Writer] = IO {
      writer.append(c)
    }

    def appendIO(cs: CharSequence): IO[Writer] = IO {
      writer.append(cs)
    }

    def appendIO(cs: CharSequence, off: Int, len: Int): IO[Writer] = IO {
      writer.append(cs, off, len)
    }

    def flushIO: IO[Unit] = IO {
      writer.flush()
    }

    def writeIO(cbuf: Array[Char]): IO[Unit] = IO {
      writer.write(cbuf)
    }

    def writeIO(cbuf: Array[Char], offset: Int, length: Int): IO[Unit] = IO {
      writer.write(cbuf, offset, length)
    }

    def writeIO(c: Int): IO[Unit] = IO {
      writer.write(c)
    }

    def writeIO(s: String): IO[Unit] = IO {
      writer.write(s)
    }

    def writeIO(s: String, offset: Int, length: Int): IO[Unit] = IO {
      writer.write(s, offset, length)
    }

  }

  /** Wraps a [[java.io.BufferedWriter]] to write char streams,so write
   *  operations are [[cats.effect.IO]] instances. If some error is detected
   *  in a write operation, the writer is closed automatically, but the error
   *  is kept by the IO instance.
   */
  implicit class BufferedWriterIO(bufferedWriter: BufferedWriter) extends WriterIO(bufferedWriter) {

    def newLineIO: IO[Unit] = IO {
      bufferedWriter.newLine()
    }

  }


  /** Wraps a [[java.io.Reader]] to read char streams,so read operations
   *  are [[cats.effect.IO]] instances. If some error is detected in a
   *  read operation, the reader is closed automatically, but the error
   *  is kept by the IO instance.
   */
  implicit class ReaderIO(reader: Reader) {

    def markIO(readAheadLimit: Int): IO[Unit] = IO {
      reader.mark(readAheadLimit)
    }

    def markSupportedIO: IO[Boolean] = IO {
      reader.markSupported()
    }

    def readIO: IO[Int] = IO {
      reader.read()
    }

    def readIO(cbuf: Array[Char], offset: Int, length: Int): IO[Int] = IO {
      reader.read(cbuf, offset, length)
    }

    def readyIO: IO[Boolean] = IO {
      reader.ready()
    }

    def resetIO: IO[Unit] = IO {
      reader.reset()
    }

    def skipIO(n: Long): IO[Long] = IO {
      reader.skip(n)
    }
  }

  /** Wraps a [[java.io.BufferedReader]] to read char streams,so read
   *  operations are [[cats.effect.IO]] instances. If some error is detected
   *  in a read operation, the reader is closed automatically, but the error
   *  is kept by the IO instance.
   */
  implicit class BufferedReaderIO(bufferedReader: BufferedReader) extends ReaderIO(bufferedReader) {

    def readLineIO: IO[String] = IO {
      bufferedReader.readLine()
    }

    def linesIO: IO[Stream[String]] = IO {
      bufferedReader.lines()
    }

  }

  object Endpoint {

    final var DEFAULT_BATCH_SIZE = 10 * 1024

    /**Copy contents of <code>origin</code> to <code>destination</code> by calling to
     * [[copyInBatchesOf]] with batch size [[DEFAULT_BATCH_SIZE]].
     */
    def copy(origin: InputStreamIO, destination: OutputStreamIO): IO[Long] =
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
    def copyInBatchesOf(origin: InputStreamIO, destination: OutputStreamIO, batchSize: Int): IO[Long] = {

      // Reads and writes in 'batches', i.e. arrays of bytes. We reuse always the same array.
      def transferLoop(origin: InputStreamIO, destination: OutputStreamIO, arr: Array[Byte], acc: Long): IO[Long] = for {
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
    def copy(origin: InputStreamIO, destination: OutputStreamIO, length: Long): IO[Long] =
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
    def copyInBatchesOf(origin: InputStreamIO, destination: OutputStreamIO, length: Long, batchSize: Int): IO[Long] = {

      def transferLoop(origin: InputStreamIO, destination: OutputStreamIO, arr: Array[Byte], acc: Long): IO[Long] =
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

    private def transfer(origin: InputStreamIO, destination: OutputStreamIO, arr: Array[Byte]): IO[Int] = 
      transfer(origin, destination, arr, 0, arr.size)

    private def transfer(origin: InputStreamIO, destination: OutputStreamIO, arr: Array[Byte], offset: Int, length: Int): IO[Int] = for {
      amount <- origin.readIO(arr, offset, length)
      _ <- if(amount > -1) destination.writeIO(arr, offset, amount)
      else IO.unit // End of read stream reached, nothing to write
    } yield amount
  }

  // TODO:
  //  - Stream data when read (Monix observable?), write data from stream
  //  - The encoding JAVA_BINARY should be improved with shapeless for automatic derivation
  //    of Encoding instances for ADTs
}
