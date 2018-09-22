package scalawithcats.chapter03_functors

import cats.Functor
import cats.Show
import cats.instances.function._ // for Functor[Function1]
import cats.instances.int._ // for Show[Int]
import cats.instances.list._ // for Functor[List], Show[List]
import cats.instances.option._ // for Functor[Option], Show[Option]
import cats.instances.string._ // for Semigroup[String], Show[String]
import cats.syntax.functor._ // for 'map'
import cats.syntax.show._ // for '.show'
import cats.syntax.semigroup._ // for |+|

import scala.concurrent.{ExecutionContext, Future}

object Main_3_5 extends App {

  val l1 = 1 :: 2 :: 3 :: Nil
  val o1 = Option(123)

  println("\n--- Functor[List] and Functor[Option] ---")
  println(Functor[List].map(l1)(_ + 1).show)
  println(Functor[Option].map(o1)(_ + 1).show)

  val f1: Int => Int = _ + 1
  val f2: Int => Int = _ * 2
  val f3: Int => String = _ + "!"
  val f4: Int => String = f1 map f2 map f3

  def doMath[F[_]: Functor](f: F[Int]): F[String] = 
    f map f4

  implicit val optionFunc: Functor[Option] = new Functor[Option] {
    override def map[A, B](ao: Option[A])(f: A => B): Option[B] = 
      ao match {
        case None => Option.empty[B]
        case Some(a) => Option(f(a))
      }
  }

  implicit def futureFunt(implicit ec: ExecutionContext): Functor[Future] = new Functor[Future] {
    override def map[A, B](fa: Future[A])(f: A => B): Future[B] =
      fa map f
  }

  sealed trait Tree[+A]
  final case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
  final case class Leaf[A](a: A) extends Tree[A]
  final case object Empty extends Tree[Nothing] {
    implicit def apply[A]: Tree[A] = Empty
  }

  implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
    override def map[A, B](ta: Tree[A])(f: A => B): Tree[B] =
      ta match {
        case Branch(tl, tr) => Branch(map(tl)(f), map(tr)(f))
        case Leaf(a) => Leaf(f(a))
        case Empty => Empty
      }
  }

  implicit def treeShow[A: Show]: Show[Tree[A]] = new Show[Tree[A]] {
    override def show(t: Tree[A]): String = 
      t match {
        case Branch(tl, tr) => tl.show |+| " ".show |+| tr.show
        case Leaf(a) => a.show
        case Empty => "(EMPTY)"
      }
  }

  println("\n--- Exercise functor tree ---")
  println((Empty: Tree[String]).show)
  println((Branch(Leaf(1), Leaf(2)): Tree[Int]).map(_ + 5))


}

