package experiments

import cats.effect.IO
import fs2.Chunk
import fs2.INothing
import fs2.Pipe
import fs2.Stream

object Experiment extends App {
  println("Now to start working...")

  implicit class Enriched[F[_], A](s: Stream[F, A]){
    def myRepeatN(i: Int): Stream[F, A] = {
      if(i <= 1) s
      else s ++ myRepeatN(i - 1)
    }
    def myDrain: Stream[F, INothing] = s.mapChunks(_ => Chunk.empty)
    def myAttempt: Stream[F, Either[Throwable, A]] = 
      s.map(Right(_): Either[Throwable, A]).handleErrorWith(t => Stream.emit(Left(t)))
    def myTakeWhile(cond: A => Boolean): Stream[F, A] = {

      def check(a: A): Pipe[F, A, A] = ???

      ???
    }
  }

  val st = Stream.eval(IO.pure(List(1,2,3)))

  println(st.myRepeatN(2).compile.fold(List.empty[Int])(_ ++ _).unsafeRunSync())
  println(st.myRepeatN(2).myDrain.compile.fold(List.empty[Int])(_ ++ _).unsafeRunSync())

}
