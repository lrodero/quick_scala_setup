package com.qvantel.miner

import org.rogach.scallop._
import scala.util.{Try, Success, Failure}

object Conf {
  val DEFAULT_FIELD_SEPARATOR = '|'
  val DEFAULT_PATHS_TO_MINE = "." :: Nil
}

class Conf(args: Seq[String]) extends ScallopConf(args) {

  import Conf._

  footer("\n (c) Qvantel. Created by Qvantel Madrid Operations, Applications Group.")
  banner("""
    |Utility to mine xdr files.
    |Usage:
    |       mine [opts] --for <regexp to mine>
    |Examples:
    |       mine --names "**.add" --for 1234 // recursively lookup for all *.add files from current folder containing 1234
    |       mine --names "*.add" --for 1234  // lookup for all *.add files in current folder containing 1234
    |       mine --cols 0 1 --names "**.add" --for 1234  // lookup for all *.add files in current folder containing 1234 in their two first columns
    """.stripMargin)
  val help = opt[Boolean](noshort=true, default=Some(false), descr="Print this message and quit.")
  val sep = opt[Char](noshort=true, default=Some(DEFAULT_FIELD_SEPARATOR), descr=s"Fields separator, defaults to '$DEFAULT_FIELD_SEPARATOR'.", argName="sep")
  val print = opt[Boolean](noshort=true, default=Some(false), descr="Print matching lines to sdtout.")
  val printWithFileName = opt[Boolean](noshort=true, default=Some(true), descr="Print matching lines to sdtout, appending to each line the file name.")
  val copyTo = opt[String](noshort=true, descr="Copy matching files to this folder.", argName="folder")
  val copyFilteredTo = opt[String](noshort=true, descr="Copy matching files to  this folder, but only write matching lines.", argName="folder")
  val forr = opt[String](noshort=true, name="for", required=true, descr="Regexp to mine (required).", argName="regexp")
  val cols = opt[List[Int]](noshort=true, descr=s"List of columns to check in each row agains the pattern. If no set all cols are checked.", argName="columns")
  val names = opt[String](noshort=true, descr="Regexp of names of files to look for inside folders, defaults to all files. Regexp is a glob expression (see http://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob)", argName="regexp")
  val paths = trailArg[List[String]](required=false, default=Some(DEFAULT_PATHS_TO_MINE), descr=s"Folders and files to mine, defaults to '${DEFAULT_PATHS_TO_MINE.mkString(", ")}'")

  mutuallyExclusive(copyTo, copyFilteredTo)
  mutuallyExclusive(print, printWithFileName)

  validateOpt (cols) {
    case Some(l) if(l.exists(_ < 0)) => Left("All cols must be > 0")
    case _ => Right(Unit)
  }

  override def onError(t: Throwable): Unit = t match {
    case _ => throw new Exception(t.getMessage) // Wrapping any error on an exception that Try can catch
  }

}

object Main extends App {

  val conf = new Conf(args)

  Try{conf.verify()} match {
    case Failure(t) => {
      if(t.getMessage != null) {
        import Console.{RED, RESET}
        Console.err.println(s"${RESET}${RED}ERROR: ${t.getMessage}${RESET}")
      }
      conf.printHelp()
    }
    case Success(_) => Miner mine conf
  }

}
