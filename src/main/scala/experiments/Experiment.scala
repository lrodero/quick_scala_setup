package experiments

import experiments.conf.Settings

import scala.util.{Try, Success, Failure}

object Experiment extends App {

  val validated: Try[Settings] = Settings.validate

  val settings: Settings = validated match {
    case Failure(t) => {
      println(t)
      System.exit(0) // Quite dirty in a real application, but what else is to do?
      validated.get  // More dirtyness, so the compiler not complains about the type returned
    }
    case Success(s) => s
  }

  println(s"${settings.bar1} ${settings.bar2}")
}
