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
}
