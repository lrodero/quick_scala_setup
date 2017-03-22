package manual.actors

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

object MyActor{
  def props: Props = Props[MyActor]
  case class Greeting(from: String)
  case object Goodbye
}

class MyActor extends Actor {
  val log = Logging(context.system, this)

  import MyActor._
  def receive = {
    case Greeting(greeter) => log.info(s"Greeted by $greeter")
    case Goodbye => log.info("Received goodbye")
  }
}
