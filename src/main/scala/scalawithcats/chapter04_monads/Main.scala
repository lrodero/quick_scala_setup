package scalawithcats.chapter04_monads

trait MyFunctor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object MyFunctorSyntax {
  implicit class MyFunctorMethods[F[_]: MyFunctor, A](fa: F[A]) {
    def map[B](f: A => B): F[B] = implicitly[MyFunctor[F]].map(fa)(f)
  }
}

trait MyMonad[F[_]] extends MyFunctor[F] {
  def pure[A](a: A): F[A]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  override def map[A, B](fa: F[A])(f: A => B) = flatMap(fa)(f andThen pure)
}

object MyMonadInstances {
  type Id[A] = A
  implicit val IdMonad = new MyMonad[Id] {
    override def pure[A](a: A): Id[A] = a
    override def flatMap[A, B](ida: Id[A])(f: A => Id[B]): Id[B] = f(ida)
  }
}

trait MyMonadError[F[_], E] extends MyMonad[F] {
  def raiseError[A](err: E): F[A]
  def handleError[A](fa: F[A])(f: E => A): F[A]
  def ensure[A](fa: F[A])(e: E)(p: A => Boolean): F[A]
}

object MyMonadSyntax {
  import MyFunctorSyntax._
  implicit class MyMonadMethods[F[_]: MyMonad, A](fa: F[A]) extends MyFunctorMethods[F, A](fa) {
    def flatMap[B](f: A => F[B]): F[B] = implicitly[MyMonad[F]].flatMap(fa)(f)
  }
  def pure[F[_]: MyMonad, A](a: A) = implicitly[MyMonad[F]].pure(a)
}

object MyMonadLaws {

  import MyMonadSyntax._
  def leftIdentity[F[_]: MyMonad, A, B](a: A, f: A => F[B]) = assert {
    pure(a).flatMap(f) == f(a)
  }
  def rightIdentity[F[_]: MyMonad, A, B](fa: F[A], f: A => B) = assert {
    fa.flatMap(a => pure(a)) == fa
  }
  def associativity[F[_]: MyMonad, A, B, C](fa: F[A], f: A => F[B], g: B => F[C]) = assert {
    fa.flatMap(f).flatMap(g) == fa.flatMap(a => f(a).flatMap(g))
  }

}

object Main extends App {

  println("---- 4.2.1 The Monad Type Class")
  import cats.Monad
  import cats.instances.option._ // for Monad[Option]
  import cats.instances.list._   // for Monad[List]
  val o2 = Monad[Option].pure(2)
  val o3 = o2.map(_ + 1)
  val l3 = Monad[List].pure(3)
  val l4 = Monad[List].flatMap(1 :: 2 :: 3 :: Nil)(i => List(i, i * 10))
  println(l4)

  println("---- 4.2.2 Monad Instances")
  import scala.concurrent.ExecutionContext.Implicits.global // required to get Monad[Future]
  import scala.concurrent.{Await, Future}
  import scala.concurrent.duration._
  import cats.instances.future._ // for Monad[Future]
  val fm = Monad[Future]
  val fu10 = fm.pure(10)
  val fu100 = fm.flatMap(fu10)(i => fm.pure(i * 10))
  println(Await.result(fu100, 1 second))

  println("---- 4.2.3 The Monad Syntax")
  import cats.syntax.applicative._ // for pure()
  import cats.syntax.functor._ // for map()
  import cats.syntax.flatMap._ // for flatMap()
  val o100 = 100.pure[Option]
  val o1000 = o100.flatMap(i => (i * 10).pure[Option])
  def sumSquare[F[_]: Monad](f1: F[Int], f2: F[Int]): F[Int] =
    f1.flatMap(i => f2.map(j => i*i + j*j))
  println(sumSquare(10.pure[Option], 5.pure[Option]))
  println(sumSquare((1 :: 2 :: 3 :: Nil), (4 :: 5 :: Nil)))

  println("---- 4.3 Identity Monad")
  import cats.Id
  println(sumSquare(10.pure[Id], 20.pure[Id]))

  println("----  4.4 Either Monad")
  import cats.syntax.either._
  val res: Either[String, Int] = for {
    i1 <- 10.asRight[String]
    i2 <- 20.asRight[String]
  } yield i1 + i2
  def countPositive(l: List[Int]): Either[String, Int] = l.foldLeft(0.asRight[String]){ (acc, value) =>
    if(value == 0) acc
    else if(value > 0) acc.map(_ + 1)
    else "Negative found".asLeft[Int]
  }
  println(countPositive(1 :: 2 :: 3 :: Nil))
  println(countPositive(-1 :: 2 :: 3 :: Nil))
  def strToInt(str: String): Either[NumberFormatException, Int] = Either.catchOnly[NumberFormatException](str.toInt)
  import scala.util.Try
  def captureExc[A, B](f: A => B): A => Either[Throwable, B] = (a: A) => Either.fromTry(Try(f(a)))
  def captureExc2[A, B](f: A => B): A => Either[Throwable, B] = (a: A) => Either.catchNonFatal(f(a))
  def readOpt[A, B](f: A => B): A => Either[Throwable, B] = (a: A) => Either.fromTry(Try(f(a)))

  println("---- 4.5 Error handling and Monad Error")
  type ErrorOr[A] = Either[String, A]
  import cats.MonadError
  import cats.instances.either._ // for MonadError]
  val me = MonadError[ErrorOr, String]
  println(me.pure(42))
  println(me.raiseError("Ups"))
  println(me.handleError(me.raiseError("crashed")) {
    case "failed" => "Good"
    case _ => me.raiseError("Can't recover")
  })

  println("---- 4.7 The writer monad")
  import cats.instances.list._ // for Monoid[Litst] (already imported above)
  import cats.data.Writer
  import cats.syntax.writer._
  import cats.syntax.applicative._ // for pure
  type Logged[A] = Writer[List[String], A]
  val w = for {
    a <- 10.pure[Logged]
    _ <- Writer.tell("here" :: "we" :: "are" :: Nil)
    b <- 23.writer("world" :: Nil)
  } yield a + b
  println(w.run)
  def slowly[A](f: => A) = try f finally Thread.sleep(100)
  import cats.Eval
  type Result[A] = Eval[Either[String, A]]
  def factorial(i: Int): Result[Int] = 
    if(i < 0) Eval.now("Cannot compute factorial of neg number".asLeft[Int])
    else if(i == 1) Eval.now(1.asRight[String])
    else Eval.defer(factorial(i-1)).map(eith => eith.map(fac => i * fac))

  type Log = List[String]
  type ResultWriter[A] = Eval[Either[String, Writer[Log, A]]]
  def factorialWriter(i: Int): ResultWriter[Int] =
    if(i < 0) Eval.now("Cannot compute factorial of neg number".asLeft[Writer[Log, Int]])
    else if(i <= 1) Eval.now((1.writer(s"Factorial of $i" :: Nil)).asRight[String])
    else Eval.defer(factorialWriter(i-1)).map(eith => eith.map(writer => writer.flatMap(faciminus1 => (faciminus1 * i).writer(s"Factorial of $i" :: Nil))))

  println(factorialWriter(5).value)
  println(factorialWriter(-1).value)
  println(factorialWriter(0).value)
  println(factorialWriter(1).value)

}
