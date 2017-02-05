package monix

object Monix extends App {
  MonixHelloWorld
  FutureHelloWorld
}

object MonixHelloWorld {

  import scala.concurrent.Await
  import scala.concurrent.duration._

  import monix.eval.Task
  import monix.execution.CancelableFuture
  import monix.execution.Scheduler.Implicits.{global => scheduler}

  val task: Task[Int] = Task{1 + 1} // Just a definition

  val cf: CancelableFuture[Int] = task.runAsync

  val tr: Int = Await.result(cf, 5.seconds)

  println(tr)

  /** First difference, tasks can be cancelled! And 'Cancelable'
   *  instances can be combined as well... */
  //// THEY DON'T WORK AS EXPECTED! (?) ////
  import monix.execution.cancelables.CompositeCancelable
  val t1: Task[Int] = Task{ Thread.sleep(3000); println("We don't intend to get here"); 1 + 1 }
  val t2: Task[Int] = Task{ Thread.sleep(3000); println("We don't intend to get here either"); 1 + 1 }
  val cf1: CancelableFuture[Int] = t1.runAsync
  val cf2: CancelableFuture[Int] = t2.runAsync
  val comp: CompositeCancelable = CompositeCancelable(cf1, cf2)
  comp.cancel()
  cf1.cancel()
  cf2.cancel()
  cf1.onSuccess { case i => println(s"cf1 Success $i") }
  cf2.onSuccess { case i => println(s"cf2 Success $i") }
  cf1.onFailure { case t => println(s"cf1 Failed $t") }
  cf2.onFailure { case t => println(s"cf2 Failed $t") }

  // TODO: Example with Task.timeout, Task.memoize
  // TODO: Check Callback and compare with Promises
  // TODO: What about Future.utils?


  /** Reactive */
  import monix.reactive._
  
  val tick =
    Observable.interval(1.second)
      .filter( _ % 2 == 0)
      .map(_ * 2)
      .flatMap(x => Observable.fromIterable(Seq(x,x)))
      .take(2)
      .dump("Out")
 
  //val cancelable = tick.subscribe()

  /** Scheduler vs ExecutionContext */
  import java.util.concurrent.TimeUnit
  import monix.execution.Cancelable

  // To have in mind: abstract class Scheduler extends ExecutionContext { ... }
  // BUT: Scheduler can execute things with a delay or periodically, also
  // it returns a token that can be used to cancel the execution
  val runnable = new Runnable {
    def run(): Unit = {
      println("Runnable - Hello, world!")
    }
  }
  scheduler.execute(runnable)
  scheduler.scheduleOnce(1, TimeUnit.SECONDS, runnable)
  val canc:Cancelable =
    scheduler.scheduleWithFixedDelay(1.seconds, 1.seconds) {runnable.run()}
  scheduler.scheduleOnce(1.seconds) {canc.cancel()}

  /** Coeval for lazy and synchronous computations.
   *  No callbacks! (it is sync, so...) */
  import monix.eval.Coeval
  import monix.eval.Coeval.Attempt
  import scala.util.Try
  val coeval: Coeval[Int] = Coeval{1 + 1}.memoize
  val cr: Try[Int] = coeval.runTry
  val cr2: Attempt[Int] = coeval.runAttempt
  assert(cr.get == cr2.get)

  // TODO: Example with Coeval.sequence

}

object FutureHelloWorld {

  import scala.concurrent.Await
  import scala.concurrent.duration._
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.{global => executor}

  val f: Future[Int] = Future {1 + 1} // Starts running immediately

  val result: Int = Await.result(f, 5.seconds)

  println(s"Futures - $result")

  val runnable = new Runnable {
    def run(): Unit = {
      println("Futures - Hello, world from executor!")
    }
  }
  executor.execute(runnable)

}


