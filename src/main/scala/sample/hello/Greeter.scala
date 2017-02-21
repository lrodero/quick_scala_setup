/**AKKA SAMPLE MAIN example as defined at
 * http://www.lightbend.com/activator/template/akka-sample-main-scala
 */
package sample.hello

import akka.actor.Actor

object Greeter {
  case object Greet
  case object Done
}

class Greeter extends Actor {
  def receive = {
    case Greeter.Greet => 
      println("Hello World")
      sender ! Greeter.Done
  }
}
