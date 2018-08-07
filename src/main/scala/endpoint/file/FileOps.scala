package endpoint.file

import endpoint._

import cats.effect.IO
import cats.implicits._

import java.io._

object FileOps {

  /**Creates an [[endpoint.ReadEndpoint]] instance by opening the
   * file passed as parameter. Recall that operations that throw
   * an error will automatically trigger the closing of the embedded
   * stream.
   */
  def openToRead(file: File): IO[ReadEndpoint] = IO {
    val is = new BufferedInputStream(new FileInputStream(file))
    new ReadEndpoint(is)
  }

  /**Creates an [[endpoint.WriteEndpoint]] instance by opening the
   * file passed as parameter. Recall that operations that throw
   * an error will automatically trigger the closing of the embedded
   * stream.
   */
  def openToWrite(file: File): IO[WriteEndpoint] = IO {
    val os = new BufferedOutputStream(new FileOutputStream(file))
    new WriteEndpoint(os)
  }

  /**Copies contents from origin to destination, returning the amount
   * of bytes read/written. This IO is cancellable, but any byte written
   * in the destination prior to cancellation will not be removed. That is,
   * cancellation interrupts the copying action, but it does not rollback
   * it.
   */
  def copy(origin: File, destination: File): IO[Long] = (openToRead(origin), openToWrite(destination))
    .tupled
    .bracket{ case (in, out) => 
      Endpoint.copy(in, out)
    }{ case (in, out) =>
      (in.close, out.close).tupled *> IO.unit
    }

  // TODO
  // Methods to read/write text content
    
  /* Wrappers for static methods of
   * https://docs.oracle.com/javase/8/docs/api/java/io/File.html
   */
  def createTempFile(prefix: String, suffix: String, directory: File): IO[File] = IO {
    File.createTempFile(prefix, suffix, directory)
  }

  def createTempFile(prefix: String, suffix: String): IO[File] = IO {
    File.createTempFile(prefix, suffix)
  }

  def listRoots: IO[List[File]] = IO {
    File.listRoots().toList
  }


  // TODO
  // Wrappers for methods in java.nio.file.Files

  /** Wrappers for all non-static methods of
   *  https://docs.oracle.com/javase/8/docs/api/java/io/File.html
   */
  implicit class FileOpsToIO(file: File) {

    def canExecute: IO[Boolean] = IO {
      file.canExecute()
    }

    def canRead: IO[Boolean] = IO {
      file.canRead()
    }

    def canWrite: IO[Boolean] = IO {
      file.canWrite()
    }

    def compareTo(pathName: File): IO[Int] = IO {
      file.compareTo(pathName)
    }

    def createNewFile: IO[Boolean] = IO {
      file.createNewFile()
    }

    def delete: IO[Boolean] = IO {
      file.delete()
    }

    def deleteOnExit: IO[Unit] = IO {
      file.deleteOnExit()
    }

    // This in fact comes from java.nio.file, but it's quite useful
    def deleteIfExists: IO[Boolean] = IO {
      java.nio.file.Files.deleteIfExists(file.toPath)
    }

    def exists: IO[Boolean] = IO {
      file.exists()
    }
 
    def getAbsoluteFile: IO[File] = IO {
      file.getAbsoluteFile()
    }

    def getAbsolutePath: IO[String] = IO {
      file.getAbsolutePath()
    }

    def getCanonicalFile: IO[File] = IO {
      file.getCanonicalFile()
    }

    def getCanonicalPath: IO[String] = IO {
      file.getCanonicalPath()
    }

    def getFreeSpace: IO[Long] = IO {
      file.getFreeSpace()
    }

    def getName: IO[String] = IO {
      file.getName()
    }

    def getParent: IO[String] = IO {
      file.getParent()
    }

    def getParentFile: IO[File] = IO {
      file.getParentFile()
    }

    def getPath: IO[String] = IO {
      file.getPath()
    }

    def getTotalSpace: IO[Long] = IO {
      file.getTotalSpace()
    }

    def getUsableSpace: IO[Long] = IO {
      file.getUsableSpace()
    }

    def isAbsolute: IO[Boolean] = IO {
      file.isAbsolute()
    }

    def isDirectory: IO[Boolean] = IO {
      file.isDirectory()
    }

    def isFile: IO[Boolean] = IO {
      file.isFile()
    }

    def isHidden: IO[Boolean] = IO {
      file.isHidden()
    }

    def lastModified: IO[Long] = IO {
      file.lastModified()
    }

    def length: IO[Long] = IO {
      file.length()
    }

    def list: IO[List[File]] = IO {
      file.listFiles.toList
    }

    def listFiles(filter: FileFilter): IO[List[File]] = IO {
      file.listFiles(filter).toList
    }

    def listFiles(filter: FilenameFilter): IO[List[File]] = IO {
      file.listFiles(filter).toList
    }

    def mkdir: IO[Boolean] = IO {
      file.mkdir()
    }

    def mkdirs: IO[Boolean] = IO {
      file.mkdirs()
    }

    def renameTo(dest: File): IO[Boolean] = IO {
      file.renameTo(dest)
    }

    def setExecutable(executable: Boolean): IO[Boolean] = IO {
      file.setExecutable(executable)
    }

    def setExecutable(executable: Boolean, ownerOnly: Boolean): IO[Boolean] = IO {
      file.setExecutable(executable, ownerOnly)
    }

    def setLastModified(time: Long): IO[Boolean] = IO {
      file.setLastModified(time)
    }

    def setReadable(readable: Boolean): IO[Boolean] = IO {
      file.setReadable(readable)
    }

    def setReadable(readable: Boolean, ownerOnly: Boolean): IO[Boolean] = IO {
      file.setReadable(readable, ownerOnly)
    }

    def setReadOnly: IO[Boolean] = IO {
      file.setReadOnly()
    }

    def setWritable(writable: Boolean): IO[Boolean] = IO {
      file.setWritable(writable)
    }

    def setWritable(writable: Boolean, ownerOnly: Boolean): IO[Boolean] = IO {
      file.setWritable(writable, ownerOnly)
    }

  }

}

