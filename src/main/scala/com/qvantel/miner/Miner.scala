package com.qvantel.miner

import org.rogach.scallop._
import better.files._
import java.io.{File => JFile}

import scala.collection.mutable

object Miner {

  private def recursiveList(path: String, namesO: Option[String]): Iterator[File] = {
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

  private def filesToMine(paths: List[String], namesO: Option[String]): List[File] =
    paths.foldLeft(mutable.MutableList.empty[File]) { (acc, path) =>
      acc ++ recursiveList(path, namesO)
    }.toList

  private def lineMatches(line: String, regexp: String, sep: String, colsO: Option[List[Int]]): Boolean = colsO match {
    case None => line.split(sep).exists((f:String) => f.matches(regexp))
    case Some(cols) => {
      val fields = line.split(sep)
      cols.filter(_ < fields.size).exists((c:Int) => fields(c).matches(regexp))
    }
  }

  private def matchingLines(file: File, regexp: String, sep: String, colsO: Option[List[Int]]): Iterator[String] =
    file.lineIterator.filter((l: String) => lineMatches(l, regexp, sep, colsO))

  private def containsMatchingLine(file: File, regexp: String, sep: String, colsO: Option[List[Int]]): Boolean = 
    file.lineIterator.contains((l: String) => lineMatches(l, regexp, sep, colsO))

  def mine(conf: Conf): Unit = {
    val paths = conf.paths()
    val namesO = conf.names.toOption

    filesToMine(paths, namesO) foreach println


  }

}

