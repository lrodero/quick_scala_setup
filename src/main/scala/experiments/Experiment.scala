package experiments

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Experiment extends App {

  val result = for {
    i <- Future{println("Starting first future"); Thread.sleep(1000); 1}
    _ <- Future.successful{println("yeah"); Thread.sleep(1000)}
    j <- Future{println("Starting second future"); 2}
  } yield (i + j)

  Await.ready(result, Duration.Inf)

  result.onComplete{ 
    case Success(i) => println(s"\nAll is good! Result is: '$i'")
    case Failure(t) => println(s"\nDoh! Error is: '$t'")
  }

}
