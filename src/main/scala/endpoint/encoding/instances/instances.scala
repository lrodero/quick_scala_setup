package endpoint.encoding.instances

import cats.effect.IO

import endpoint.encoding.Encoding

import scala.util.Try

import java.io._

/**Binary encoding of AnyVal instances (Int, Boolean...) as they are
 * represented in Java.
 */
object JAVA_BINARY {

  private def readFromDataInputStream[A](is: InputStream, f: DataInputStream => A): IO[A] = IO {
    Try {
      f(is.asInstanceOf[DataInputStream])
    }.recover{
      case _ => f(new DataInputStream(is))
    }.get
  }

  private def writeToDataOutputStream[Unit](os: OutputStream, f: DataOutputStream => Unit): IO[Unit] = IO {
    Try {
      f(os.asInstanceOf[DataOutputStream])
    }.recover{
      case _ => f(new DataOutputStream(os))
    }.get
  }

  implicit object EncInt extends Encoding[Int] {
    def read(is: InputStream): IO[Int] =
      readFromDataInputStream(is, _.readInt())
      
    def write(i: Int, os: OutputStream): IO[Unit] = 
      writeToDataOutputStream(os, _.writeInt(i))
  }

  implicit object EncShort extends Encoding[Short] {
    def read(is: InputStream): IO[Short] =
      readFromDataInputStream(is, _.readShort())

    def write(s: Short, os: OutputStream): IO[Unit] =
      writeToDataOutputStream(os, _.writeShort(s))
  }

  implicit object EncLong extends Encoding[Long] {
    def read(is: InputStream): IO[Long] =
      readFromDataInputStream(is, _.readLong())

    def write(l: Long, os: OutputStream): IO[Unit] =
      writeToDataOutputStream(os, _.writeLong(l))
  }

  implicit object EncDouble extends Encoding[Double] {
    def read(is: InputStream): IO[Double] =
      readFromDataInputStream(is, _.readDouble())

    def write(d: Double, os: OutputStream): IO[Unit] =
      writeToDataOutputStream(os, _.writeDouble(d))
  }

  implicit object EncFloat extends Encoding[Float] {
    def read(is: InputStream): IO[Float] =
      readFromDataInputStream(is, _.readFloat())

    def write(f: Float, os: OutputStream): IO[Unit] =
      writeToDataOutputStream(os, _.writeFloat(f))
  }

  implicit object EncByte extends Encoding[Byte] {
    def read(is: InputStream): IO[Byte] =
      readFromDataInputStream(is, _.readByte)

    def write(b: Byte, os: OutputStream): IO[Unit] =
      writeToDataOutputStream(os, _.writeByte(b))
  }

  implicit object EncBoolean extends Encoding[Boolean] {
    def read(is: InputStream): IO[Boolean] =
      readFromDataInputStream(is, _.readBoolean())

    def write(b: Boolean, os: OutputStream): IO[Unit] =
      writeToDataOutputStream(os, _.writeBoolean(b))
  }

  implicit object EncChar extends Encoding[Char] {
    def read(is: InputStream): IO[Char] =
      readFromDataInputStream(is, _.readChar())

    def write(c: Char, os: OutputStream): IO[Unit] =
      writeToDataOutputStream(os, _.writeChar(c))
  }

  implicit object EncString extends Encoding[String] {
    def read(is: InputStream): IO[String] =
      readFromDataInputStream(is, _.readUTF())

    def write(s: String, os: OutputStream): IO[Unit] =
      writeToDataOutputStream(os, _.writeUTF(s))
  }

}
