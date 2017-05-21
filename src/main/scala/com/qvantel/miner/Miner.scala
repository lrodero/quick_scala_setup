package com.qvantel.miner

import org.rogach.scallop._
import better.files._
import better.files.Dsl._
import java.io.{File => JFile}

import scala.collection.mutable
import scala.util.Try

object Miner {

  /**Compute list of files to 'mine'. For each path, if it is a folder
   * it recursively traverses it (mathing each found file with the 'names'
   * regexp given if any), if it is a file it just adds it.
   */
  private def filesToMine(paths: List[String], namesO: Option[String]): List[File] = {

    /**Returns files found under the given path. If a regexp is given
     * (names param) then only files with names matching the param are returned.
     */
    def recursiveList(path: String, namesO: Option[String]): Iterator[File] = {
      val file = File(path)
      if(!file.isDirectory) {
        (file :: Nil).iterator
      } else {
        namesO match {
          case None => file.listRecursively
          case Some(names) => file.glob(names)
        }
      }
    }

    paths.foldLeft(mutable.MutableList.empty[File]) { (acc, path) =>
      acc ++ recursiveList(path, namesO)
    }.filter(!_.isDirectory).toList
  }

  /**Returns true if it the line contains any field (line is splitted using given sep) that matches the regexp. If cols is defined only fields in those cols are checked.
   */
  private def lineMatches(line: String, regexp: String, sep: String, colsO: Option[List[Int]]): Boolean = colsO match {
    case None => line.split(sep).exists((f:String) => f.matches(regexp))
    case Some(cols) => {
      val fields = line.split(sep)
      cols.filter(_ < fields.size).exists((c:Int) => fields(c).matches(regexp))
    }
  }

  /** Returns all matching lines in file */
  private def matchingLines(file: File, regexp: String, sep: String, colsO: Option[List[Int]]): Iterator[String] = {
    Try{file.lineIterator.filter((l: String) => lineMatches(l, regexp, sep, colsO))}
      .recover{case (_:Throwable) => List.empty[String].toIterator}.get
  }

  /** Returns true if file contains at least one matching line */
  private def containsMatchingLine(file: File, regexp: String, sep: String, colsO: Option[List[Int]]): Boolean = 
    file.lineIterator.contains((l: String) => lineMatches(l, regexp, sep, colsO))

  /**'Core' of the miner, this funcion does the mining: it first computes
   * the list of files to mine and then checks them against the regexp
   * looked for.
   */
  def mine(conf: Conf): Unit = {
    
    /* First we get the list of files to mine */
    val paths: List[String] = conf.paths()
    val namesO: Option[String] = conf.names.toOption
    val files = filesToMine(paths, namesO)

    /* Now we start mining file by file */
    val regexp: String = conf.regexp()
    val sep: String = conf.sep()
    val colsO: Option[List[Int]] = conf.cols.toOption
    val copyFilteredToO: Option[String] = conf.copyTo.toOption
    val copyToO: Option[String] = conf.copyTo.toOption

    for(file <- files) {
      /* If 'print' or 'copyFilteredTo' are set, then we are
       * interested in the matching lines of each file, if any.
       */
      val destFilteredFileO = copyFilteredToO.map(destFolder => destFolder/file.name)
      for {
        destFile <- destFilteredFileO
        line <- matchingLines(file, regexp, sep, colsO)
      } destFile append line

      /* If 'copy', then it is enough to know whether each
       * file contains any mathing line or not.
       */
      val destFileO = copyToO.map(destFolder => destFolder/file.name)
      for {
        destFile <- destFileO
        if(containsMatchingLine(file, regexp, sep, colsO))
      } cp(file, destFile)
    }
  }
}
