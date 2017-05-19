package com.qvantel.miner

import org.rogach.scallop._
import scala.util.{Try, Success, Failure}

object Conf {
  val DEFAULT_FIELD_SEPARATOR = '|'
  val DEFAULT_PATHS_TO_MINE = "." :: Nil
}

class Conf(args: Seq[String]) extends ScallopConf(args) {

  import Conf._

  footer("\n (c) Qvantel. Created by Qvantel Madrid Operations, Applications Group")
  banner("Usage: mine [opts] --for <string or regexp to mine>")
  val help = opt[Boolean](default=Some(false), descr="Print this message and quit")
  val sep = opt[Char](default=Some(DEFAULT_FIELD_SEPARATOR), descr=s"Fields separator, defaults to '$DEFAULT_FIELD_SEPARATOR'", argName="sep")
  val norec = opt[Boolean](default=Some(false), descr="Turn off recursion when exploring folders")
  val regexp = opt[Boolean](required=false, descr="String to look for is s regexp")
  val lines = opt[Boolean](default=Some(true), descr="Print matching lines to sdtout (default)")
  val linesWithFilename = opt[Boolean](short='L', default=Some(true), descr="Print matching lines to sdtout, appending to each line the file name")
  val copy = opt[String](required=false, descr="Copy matching files to folder", argName="folder")
  val copyFilter = opt[String](short='C', required=false, descr="Copy matching files to folder, but only write matching lines", argName="folder")
  val forr = opt[String](short='f', name="--for", required=true, descr="String or regexp to mine (required)")
  val paths = trailArg[List[String]](required=false, default=Some(DEFAULT_PATHS_TO_MINE), descr=s"Folders and files to minei, defaults to '${DEFAULT_PATHS_TO_MINE.mkString(", ")}'")

  mutuallyExclusive(copy, copyFilter)
  mutuallyExclusive(lines, linesWithFilename)

  override def onError(t: Throwable): Unit = t match {
    case _ => throw new Exception(t.getMessage) // Wrapping any error on an exception that Try can catch
  }

}

object Main extends App {

  val conf = new Conf(args)

  import Console.{RED, RESET}
  Try{conf.verify()} match {
    case Failure(t) => {Console.err.println(s"${RESET}${RED}ERROR: ${t.getMessage}${RESET}"); conf.printHelp()}
    case Success(_) => ()
  }

}
