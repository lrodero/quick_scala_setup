# Quick Scala Setup

This lib is an experiment to gain experience with cats effect library, specially with IO type.

The goal is to create a set of utils to access 'endpoints', and endpoint being an abstraction of any entity data can be read/written from/to.

Once given an endpoint, a Java's `BufferedReader` or `BufferedWriter` will be created to access to it. From there, using IO data will be read/written. 

```scala

val ep = Endpoint("file://whatever.txt")

trait ToByteArray[A] {
  implicit def toByteArray(a: A): Either[Throwable, ByteArray]
}

trait FromByteArray[A] {
  def readDataInputStream(dis: DataInputStream): Either[Throwable, A]
}

def read[A: FromByteArray]: IO[A]
def write[A: ToByteArray](a: A): IO[Unit]



val readLine: IO[String] = ep.readLine
def writeLine(s: String): IO[Unit] = ep.writeLine(s)

```
