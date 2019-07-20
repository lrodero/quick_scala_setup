package scalawithcats.chapter06_semigroupal_and_applicative

import cats.syntax.either._

trait MySemigroupal[F[_]] {
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
}

trait MyFunctor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object MyFunctorSyntax {
  implicit class Syntax[F[_]: MyFunctor, A](fa: F[A]) {
    def map[B](f: A => B) = implicitly[MyFunctor[F]].map(fa)(f)
  }
}

object MyFunctorInstances {
  implicit object MFOption extends MyFunctor[Option] {
    override def map[A, B](oa: Option[A])(f: A => B): Option[B] = oa.map(f)
  }
}

object MySemigroupal {
  import MyFunctorSyntax._
  def product[F[_]: MySemigroupal, A, B](fa: F[A], fb: F[B]): F[(A, B)] =
    implicitly[MySemigroupal[F]].product(fa, fb)
  def tuple3[F[_]: MySemigroupal: MyFunctor, A, B, C](fa: F[A], fb: F[B], fc: F[C]): F[(A, B, C)] = {
    val tupleOf2: F[(A, B)] = product(fa, fb)
    val twoTuples: F[((A, B), C)] = product(tupleOf2, fc)
    twoTuples.map({(triad: ((A, B), C)) => (triad._1._1, triad._1._2, triad._2)})
  }
  def map2[F[_]: MySemigroupal: MyFunctor, A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    product(fa, fb).map(f.tupled)
  def map3[F[_]: MySemigroupal: MyFunctor, A, B, C, D](fa: F[A], fb: F[B], fc: F[C])(f: (A, B, C) => D): F[D] =
    tuple3(fa, fb, fc).map(f.tupled)
}

object MySemigroupalSyntax {
  implicit class Syntax[F[_]: MySemigroupal, A](fa: F[A]) {
    def product[B](fb: F[B]): F[(A,B)] = implicitly[MySemigroupal[F]].product(fa, fb)
  }

  implicit class Syntax2[F[_]: MySemigroupal, A, B](fab: (F[A], F[B])) {
    def tupled: F[(A,B)] = implicitly[MySemigroupal[F]].product(fab._1, fab._2)
    def mapN[C](f: (A, B) => C)(implicit mf: MyFunctor[F]): F[C] = MySemigroupal.map2(fab._1, fab._2)(f)
  }
}

object MySemigroupalInstances {
  implicit object MSOption extends MySemigroupal[Option] {
    override def product[A, B](oa: Option[A], ob: Option[B]): Option[(A, B)] =
      (oa, ob) match {
        case (Some(a), Some(b)) => Option((a, b))
        case _ => None
      }
  }
}

trait MySemigroup[A] {
  def combine(a1: A, a2: A): A
}

trait MyMonoid[A] extends MySemigroup[A] {
  def zero: A
}

object MyMonoidInstances {
  implicit object MyMonoidInt extends MyMonoid[Int] {
    override def zero = 0
    override def combine(i1: Int, i2: Int) = i1 + i2
  }
  implicit object MyMonoidString extends MyMonoid[String] {
    override def zero = ""
    override def combine(s1: String, s2: String) = s1 + s2
  }
}

object MyMonoidSyntax {
  implicit class Syntax[A: MyMonoid](a: A) {
    def combine(a2: A): A = implicitly[MyMonoid[A]].combine(a, a2)
    def |+|(a2: A): A = combine(a2)
  }
}



trait MyMonoidal[F[_]] {
  def zero[A]: F[A]
  def combine[A](fa1: F[A], fa2: F[A]): F[A]
}

trait MyMonad[F[_]] extends MyFunctor[F] with MySemigroupal[F] {
  def pure[A](a: A): F[A]
  def flatMap[A, B](fa: F[A])(fab: A => F[B]): F[B]
  override def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(a => pure(f(a)))
  override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] = flatMap(fa)(a => map(fb)(b => (a, b)))
}

sealed abstract class MyValidated[+L, +R]
final case class MyValid[R](value: R) extends MyValidated[Nothing, R] 
final case class MyInvalid[L](value: L) extends MyValidated[L, Nothing]

object MyValidatedSyntax {
  implicit class Syntax[A](a: A) {
    def valid[L]: MyValidated[L, A] = MyValid(a)
    def invalid[R]: MyValidated[A, R] = MyInvalid(a)
  }
}

object MyValidatedInstances {
  implicit def semigroup[L: MySemigroup, R: MySemigroup]: MySemigroup[MyValidated[L, R]] = new MySemigroup[MyValidated[L,R]] {
    override def combine(mv1: MyValidated[L,R], mv2: MyValidated[L,R]): MyValidated[L,R] = (mv1, mv2) match {
      case (MyValid(r1), MyValid(r2)) => MyValid(implicitly[MySemigroup[R]].combine(r1, r2))
      case (MyInvalid(l1), MyInvalid(l2)) => MyInvalid(implicitly[MySemigroup[L]].combine(l1, l2))
      case (mi@MyInvalid(_), _) => mi
      case (_, mi@MyInvalid(_)) => mi

    }
  }
  class MyValidatedSemigroupal[E: MySemigroup] extends MySemigroupal[MyValidated[E, *]] {
    override def product[A, B](mva: MyValidated[E, A], mvb: MyValidated[E, B]): MyValidated[E, (A, B)] = (mva, mvb) match {
      case (MyValid(a), MyValid(b)) => MyValid((a,b))
      case (MyInvalid(e1), MyInvalid(e2)) => MyInvalid(implicitly[MySemigroup[E]].combine(e1, e2))
      case (mi@MyInvalid(_), _) => mi
      case (_, mi@MyInvalid(_)) => mi
    }
  }
}

object Main extends App {

  import MySemigroupalSyntax._
  import MySemigroupalInstances._

  println("--- 6.1.1 Joining two contexts")
  println(Option(1) product Option(2))
  println(Option(123) product Option("123"))
  println(Option(1234) product Option.empty[Int])

  println("--- 6.1.2 Joining two or more contexts")
  import MyFunctorInstances._
  println(MySemigroupal.tuple3(Option(1), Option(2), Option(3)))
  println(MySemigroupal.map3(Option(1), Option(2), Option(3))(_ + _ + _))
  println(MySemigroupal.map2(Option(1), Option.empty[Int])(_ + _))
  println((Option(3), Option("4")).tupled)
  println((Option(3), Option(4)).mapN(_ + _))
  println((Option(3), Option("4")).mapN(_ + _))
  println((Option(3), Option.empty[String]).mapN(_ + _))

  println("--- 6.4.4 Exercise")
  case class User(name: String, age: Int)
  def getValue(key:String)(map: Map[String, String]): Either[List[String], String] =
    map.get(key).toRight(s"Could not find key $key" :: Nil)
  import cats.syntax.either._
  def parseInt(s: String): Either[String, Int] =
    Either.catchOnly[NumberFormatException](s.toInt).leftMap(_ => s"Could not transform '$s' into an Int")
  def nonBlank(s: String): Either[String, String] =
    Either.cond(!s.trim.isEmpty, s, "String is blank")
  def nonNegative(i: Int): Either[String, Int] =
    Either.cond(i >= 0, i, s"'$i' cannot be negative")
  def readName(map: Map[String, String]): Either[String, String] =
    (for {
      name <- getValue("Name")(map)
      _    <- nonBlank(name)
    } yield name).leftMap(err => s"Could not read name: $err")
  def readAge(map: Map[String, String]): Either[String, Int] = 
    (for {
      ageS <- getValue("Age")(map)
      age  <- parseInt(ageS)
      _    <- nonNegative(age)
    } yield age).leftMap(err => s"Could not read age: $err")
  import cats.data.Validated
  import cats.instances.list._ // for Monoid[List]
  import cats.syntax.apply._ // map2
  def createUser(map: Map[String, String]): Validated[List[String], User] =
    (Validated.fromEither(readName(map).leftMap(_ :: Nil)), Validated.fromEither(readAge(map).leftMap(_ :: Nil))).mapN(User)

  val goodMap = Map("Name" -> "Pepe", "Age" -> "40")
  val badMap1 = Map("Name" -> "", "Age" -> "40")
  val badMap2 = Map("Name" -> "Pepe", "Age" -> "-1")
  val badMap3 = Map("Name" -> "", "Age" -> "-1")
  println(createUser(goodMap))
  println(createUser(badMap1))
  println(createUser(badMap2))
  println(createUser(badMap3))


}
