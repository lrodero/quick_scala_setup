package endpoint.encoding

import cats.effect.IO

import java.io._

/**
 * Wraps with IO instances read/writing actions on the provided streams. No action is taken
 * in case of error, e.g. the stream is not closed. Is the caller who better knows what to
 * do when errors are triggered.
 */
trait Encoding[A] {
  def read(dis: InputStream): IO[A]
  def write(a: A, dos: OutputStream): IO[Unit]
}
