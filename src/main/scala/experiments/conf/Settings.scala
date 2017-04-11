package experiments.conf

import scala.util.Try

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by lrodero on 11/04/2017.
  */
case class Settings private (private val conf: Config) {
  // non-lazy fields, we want all exceptions at construct time
  val bar1: Int = conf.getInt("foo.bar")
  val foo: Config = conf.getConfig("foo")
  val bar2: Int = foo.getInt("bar2") // same as conf.getInt("foo.bar2")
}

object Settings {
  def validate: Try[Settings] = validate(ConfigFactory.load(), ConfigFactory.defaultReference())
  def validate(conf: Config, ref: Config): Try[Settings] = Try {
    conf.checkValid(ref)
    Settings(conf)
  }
}
