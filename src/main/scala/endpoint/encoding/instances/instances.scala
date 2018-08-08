package endpoint.encoding.instances

import cats.effect.IO

import endpoint.encoding.Encoding

import java.io._

/**Binary encoding of AnyVal instances (Int, Boolean...) as they are
 * represented in Java.
 */
object JAVA_BINARY {

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
