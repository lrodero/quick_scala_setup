package experiments

import java.util.UUID

import cats.data.StateT
import cats.effect.IO
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._

class Experiment[F[_]: Sync] extends App {

  val status = new Status[IO, UUID, String]

  val state = StateT[F, Map[String, Int], Map[String, Int]]

}

/**
  * Why not StateT[F, Map, Map]? Well, because StateT is not safe for concurrent accesses
  * as it does not provides synchronization.
  */
final class Status[F[_]: Sync, A, B](map: Map[A, B] = Map.empty[A,B]) {
  val stateF: F[Ref[F, Map[A,B]]] = Ref.of[F, Map[A, B]](map)
  def update(f: Map[A,B] => Map[A,B]): F[Unit] =
    stateF >>= { state =>
      state.modify { map =>
         (f(map) , ())
      }
    }
  def read[C](f: Map[A,B] => C): F[C] =
    stateF >>= { state =>
      state.get.map(f(_))
    }
}
