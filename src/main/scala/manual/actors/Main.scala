package manual.actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.DeadLetter
import akka.actor.Props
import akka.event.Logging

object Main extends App {
  val system = ActorSystem("mySystem")
  val firstActor = system.actorOf(Props[FirstActor], "firstactor")
  val deadLettersHandler = system.actorOf(Props[DeadLettersHandler], "deadLettersHandler")
  system.terminate()
}

class FirstActor extends Actor {
  val log = Logging(context.system, this)
  val child = context.actorOf(MyActor.props, name = "myChild")
  child ! MyActor.Greeting("first actor")
  def receive = {
    case _ => log.info("Received something")
  }
}

class DeadLettersHandler extends Actor {
  val log = Logging(context.system, this)
  context.system.eventStream.subscribe(self, classOf[DeadLetter])
  def receive = {
    case DeadLetter(msg,sender,recipient) => log.info(s"Received dead letter '$msg' from ${sender.path.name} to ${recipient.path.name}")
  }
}
