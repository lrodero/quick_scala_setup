package scalawithcats.chapter07_foldable_and_traverse

object Main extends App {

  println("--- 7.1.1 Folds and folding")
  def show[A](l: List[A]) = 
    l.foldLeft("")((acc, s) => acc + s)
  println(show(1 :: 2 :: 3 :: Nil))

  def mapFromFoldRight[A, B](la: List[A])(f: A => B): List[B] =
    la.foldRight(List.empty[B])((a, acc) => f(a) :: acc)
  println(mapFromFoldRight(1 :: 2 :: 3 :: Nil)(_ * 100))
  def flatMapFromFoldRight[A, B](la: List[A])(f: A => List[B]): List[B] =
    la.foldRight(List.empty[B])((a, acc) => f(a) ++ acc)
  println(flatMapFromFoldRight(1 :: 2 :: 3 :: Nil)(i => i*10 :: i * 100 :: Nil))
  def filterFromFoldRight[A](la: List[A])(p: A => Boolean): List[A] =
    la.foldRight(List.empty[A]){(a, acc) => if (p(a)) a :: acc else acc}
  println(filterFromFoldRight(1 :: 2 :: 3 :: Nil)(_ % 2 == 0))
  import cats.Monoid
  def sumFromFoldRight[A: Monoid](la: List[A]): A = {
    val monoid = implicitly[Monoid[A]]
    la.foldRight(monoid.empty)((a, acc) => monoid.combine(acc, a))
  }
  import cats.instances.int._ // Monoid[Int]
  println(sumFromFoldRight(1 :: 2 :: 3 :: Nil))

}
