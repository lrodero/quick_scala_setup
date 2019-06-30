package scalawithcats.chapter03_functors

trait MyFunctor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]

  object Syntax {
    implicit class Methods[F[_]: MyFunctor, A](fa: F[A]) {
      def map[B](f: A => B) = implicitly[MyFunctor[F]].map(fa)(f)
    }
  }

  object Laws {
    import Syntax._
    def identity[F[_]: MyFunctor, A](fa:F[A]) = fa.map(a => a) == fa
    def composition[F[_]: MyFunctor, A, B, C](fa: F[A])(f: A => B, g: B => C) = fa.map(f).map(g) == fa.map(f andThen g)
  }
}

object Main extends App {

  println("3.5.1 The Functor type class")
  import cats.Functor
  import cats.instances.list._
  import cats.instances.option._

  val l1 = 1 :: 2 :: 3 :: Nil
  val o1 = Option(1)

  println(Functor[List].map(l1)(_ + 1))
  println(Functor[Option].map(o1)(_ + 1))

  val letsLift: Int => Int = _ * 10

  val lifted: Option[Int] => Option[Int] = Functor[Option].lift(letsLift)
  println(lifted(o1))

  println("3.5.2 The Functor syntax")
  import cats.instances.function._
  import cats.syntax.functor._

  val f1: Int => Int = _ + 1
  val f2: Int => Int = _ * 2
  val f3: Int => String = _ + "!"
  val f4 = f1 map f2 map f3
  println(f4(123))

  def doMath[F[_]: Functor](start: F[Int]) =
    start.map(n => n + 1 * 2)
  println(doMath(l1))
  println(doMath(o1))

  println("3.5.4 Exercise Branching out with functors")
  sealed trait Tree[+A]
  final case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
  final case class Leaf[A](value: A) extends Tree[A]
  final case object EmptyLeaf extends Tree[Nothing]

  implicit object TreeFunctor extends Functor[Tree] {
    override def map[A, B](ta: Tree[A])(f: A => B): Tree[B] = ta match {
      case Branch(l, r) => Branch(map(l)(f), map(r)(f))
      case Leaf(a) => Leaf(f(a))
      case EmptyLeaf => EmptyLeaf
    }
  }

  val tree: Tree[Int] = Branch(Leaf(1), Leaf(2))
  println(tree.map(_ * 2))

  println("3.6.1 Contravariant functors")

  // Keep this in your mind: contravariant only makes sense for types that represent 'transformations'
  trait MyPrintable[A] { self =>
    def format(a: A): String
    def contramap[B](f: B => A): MyPrintable[B] = new MyPrintable[B] {
      def format(b: B) = self.format(f(b))
    }
  }

  object MyPrintableSyntax {
    implicit class MyPrintableMethods[A: MyPrintable](a: A) {
      def format: String = implicitly[MyPrintable[A]].format(a)
    }
  }

  object MyPrintableInstances {
    implicit val myPrintableDouble = new MyPrintable[Double] {
      override def format(d: Double) = d.toString
    }
    implicit val myPrintableString = new MyPrintable[String] {
      override def format(s: String) = s
    }
  }

  import MyPrintableSyntax._
  import MyPrintableInstances._
  println(10.0.format)
  implicit val myPrintableInt: MyPrintable[Int] = implicitly[MyPrintable[Double]].contramap(d => d.toInt)
  println(100.format)

  final case class Box[A](a: A)
  implicit def myPrintable[A: MyPrintable]: MyPrintable[Box[A]] = new MyPrintable[Box[A]] {
    def format(ba: Box[A]): String = s"ThisIsABox[${ba.a.format}]"
  }
  println(Box(10).format)
  println(Box("hello world").format)

  println("3.6.2 Invariant types with imap")
  trait MyContravariant[F[_]] {
    def contramap[A,B](fb: F[B])(f: A => B): F[A]
  }
  trait MyInvariant[F[_]] extends MyFunctor[F] with MyContravariant[F] {
    def imap[A,B](fa: F[A])(fab: A => B, fba: B => A): F[B]
  }

  trait MyCodec[A] { self =>
    def fromString(s: String): A
    def toString(a: A): String
    def imap[B](fab: A => B, fba: B => A) = new MyCodec[B] {
      override def fromString(s: String): B = fab(self.fromString(s))
      override def toString(b: B) = self.toString(fba(b))
    }
  }

  object CodecInstances {
    implicit val stringCodec = new MyCodec[String] {
      def fromString(s: String) = s
      def toString(s: String) = s
    }
    implicit val booleanCodec = stringCodec.imap[Boolean](_.toBoolean, _.toString)
  }




}

