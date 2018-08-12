package endpoint.file

import endpoint.Endpoint._

import cats.effect.IO
import cats.implicits._

import java.io._

/** Set of operations that create [[endpoint.ReadEndpoint]] and 
 *  [[endpoint.WriteEndpoint]] instances to access files. They
 *  are created by wrapping instances of java streams that operate
 *  on those files.
 */
object FileIO {

  def openToReadBinIO(file: File): IO[BufferedInputStream] = IO {
    new BufferedInputStream(new FileInputStream(file))
  }

  def openToReadTxtIO(file: File): IO[FileReader] = IO {
    new FileReader(file)
  }
  
  def openToWriteBinIO(file: File): IO[BufferedOutputStream] = IO {
    new BufferedOutputStream(new FileOutputStream(file))
  }

  def openToWriteTxtIO(file: File): IO[FileWriter] = IO {
    new FileWriter(file)
  }

  /**Copies contents from origin to destination, returning the amount
   * of bytes read/written. This IO is cancellable, but any byte written
   * in the destination prior to cancellation will not be removed. That is,
   * cancellation interrupts the copying action, but it does not rollback
   * it.
   */
  def copy(origin: File, destination: File): IO[Long] = (openToReadBinIO(origin), openToWriteBinIO(destination))
    .tupled
    .bracket{ case (in, out) => 
      Endpoint.copy(in, out)
    }{ case (in, out) =>
      (in.closeIO, out.closeIO).tupled *> IO.unit
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

  def listRoots: IO[Option[List[File]]] = IO {
    Option(File.listRoots()).map(_.toList)
  }


  // TODO
  // Wrappers for methods in java.nio.file.Files

  /** Wrappers for all non-static methods of
   *  https://docs.oracle.com/javase/8/docs/api/java/io/File.html
   */
  implicit class FileIOOps(file: File) {

    def canExecuteIO: IO[Boolean] = IO {
      file.canExecute()
    }

    def canReadIO: IO[Boolean] = IO {
      file.canRead()
    }

    def canWriteIO: IO[Boolean] = IO {
      file.canWrite()
    }

    def compareToIO(pathName: File): IO[Int] = IO {
      file.compareTo(pathName)
    }

    def createNewFileIO: IO[Boolean] = IO {
      file.createNewFile()
    }

    def deleteIO: IO[Boolean] = IO {
      file.delete()
    }

    def deleteOnExitIO: IO[Unit] = IO {
      file.deleteOnExit()
    }

    // This in fact comes from java.nio.file, but it's quite useful
    def deleteIfExistsIO: IO[Boolean] = IO {
      java.nio.file.Files.deleteIfExists(file.toPath)
    }

    def existsIO: IO[Boolean] = IO {
      file.exists()
    }
 
    def getAbsoluteFileIO: IO[File] = IO {
      file.getAbsoluteFile()
    }

    def getAbsolutePathIO: IO[String] = IO {
      file.getAbsolutePath()
    }

    def getCanonicalFileIO: IO[File] = IO {
      file.getCanonicalFile()
    }

    def getCanonicalPathIO: IO[String] = IO {
      file.getCanonicalPath()
    }

    def getFreeSpaceIO: IO[Long] = IO {
      file.getFreeSpace()
    }

    def getNameIO: IO[String] = IO {
      file.getName()
    }

    def getParentIO: IO[String] = IO {
      file.getParent()
    }

    def getParentFileIO: IO[File] = IO {
      file.getParentFile()
    }

    def getPathIO: IO[String] = IO {
      file.getPath()
    }

    def getTotalSpaceIO: IO[Long] = IO {
      file.getTotalSpace()
    }

    def getUsableSpaceIO: IO[Long] = IO {
      file.getUsableSpace()
    }

    def isAbsoluteIO: IO[Boolean] = IO {
      file.isAbsolute()
    }

    def isDirectoryIO: IO[Boolean] = IO {
      file.isDirectory()
    }

    def isFileIO: IO[Boolean] = IO {
      file.isFile()
    }

    def isHiddenIO: IO[Boolean] = IO {
      file.isHidden()
    }

    def lastModifiedIO: IO[Long] = IO {
      file.lastModified()
    }

    def lengthIO: IO[Long] = IO {
      file.length()
    }

    def listIO: IO[List[File]] = IO {
      file.listFiles.toList
    }

    def listFilesIO(filter: FileFilter): IO[List[File]] = IO {
      file.listFiles(filter).toList
    }

    def listFilesIO(filter: FilenameFilter): IO[List[File]] = IO {
      file.listFiles(filter).toList
    }

    def mkdirIO: IO[Boolean] = IO {
      file.mkdir()
    }

    def mkdirsIO: IO[Boolean] = IO {
      file.mkdirs()
    }

    def renameToIO(dest: File): IO[Boolean] = IO {
      file.renameTo(dest)
    }

    def setExecutableIO(executable: Boolean): IO[Boolean] = IO {
      file.setExecutable(executable)
    }

    def setExecutableIO(executable: Boolean, ownerOnly: Boolean): IO[Boolean] = IO {
      file.setExecutable(executable, ownerOnly)
    }

    def setLastModifiedIO(time: Long): IO[Boolean] = IO {
      file.setLastModified(time)
    }

    def setReadableIO(readable: Boolean): IO[Boolean] = IO {
      file.setReadable(readable)
    }

    def setReadableIO(readable: Boolean, ownerOnly: Boolean): IO[Boolean] = IO {
      file.setReadable(readable, ownerOnly)
    }

    def setReadOnlyIO: IO[Boolean] = IO {
      file.setReadOnly()
    }

    def setWritableIO(writable: Boolean): IO[Boolean] = IO {
      file.setWritable(writable)
    }

    def setWritableIO(writable: Boolean, ownerOnly: Boolean): IO[Boolean] = IO {
      file.setWritable(writable, ownerOnly)
    }

  }

}

