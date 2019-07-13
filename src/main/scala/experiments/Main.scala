package experiments

import cats.free.Free
import cats.{Id, ~>}
import cats.data.State
import java.util.UUID

sealed trait KVStore[V]
final case class Put[V](k: UUID, v: V) extends KVStore[Unit]
final case class Get[V](k: UUID) extends KVStore[Option[V]]
final case class Delete(k: UUID) extends KVStore[Boolean]

object Main extends App {

  /* ******** */
  /* LANGUAGE */
  /* ******** */
  type FKVStore[V] = Free[KVStore, V]

  def put[V](k: UUID, v: V): FKVStore[Unit] = Free.liftF[KVStore, Unit](Put(k, v))
  def get[V](k: UUID): FKVStore[Option[V]] = Free.liftF[KVStore, Option[V]](Get(k))
  def delete(k: UUID): FKVStore[Boolean] = Free.liftF[KVStore, Boolean](Delete(k))
  val noOp: FKVStore[Unit] = Free.pure[KVStore, Unit](())
  def update[V](k: UUID, f: V => V): FKVStore[Boolean] =
    for {
      existsO <- get[V](k)
      _       <- existsO match {
        case Some(oldV) => put[V](k, f(oldV))
        case None       => noOp
      }
    } yield existsO.isDefined

  /* *************** */
  /* EXAMPLE PROGRAM */
  /* *************** */

  val program: FKVStore[Option[Int]] = {

    val id1 = UUID.randomUUID()
    val id2 = UUID.randomUUID()

    for {
      _ <- put(id1, 2)
      _ <- update[Int](id1, (_ + 12))
      _ <- put(id2, 5)
      n <- get[Int](id1)
      _ <- delete(id2)
    } yield n
  }

  /* ********* */
  /* COMPILERS */
  /* ********* */
  // Realize that compilers are totally independent of the Free constructors created above! //
  def impureCompiler: KVStore ~> Id = new (KVStore ~> Id) {

    val store = scala.collection.mutable.Map.empty[UUID, Any]

    def apply[V](kvStore: KVStore[V]): Id[V] = kvStore match {
      case Put(k, v) => store += (k -> v); ()
      case Get(k)    => store.get(k)
      case Delete(k) => store.remove(k).isDefined
    }

  }

  import cats.Eval
  import cats.data.IndexedStateT
  type MapKV = Map[UUID, Any]
  type StateMap[V] = IndexedStateT[Eval, MapKV, MapKV, V]
  def pureCompiler: KVStore ~> StateMap = new (KVStore ~> StateMap ) {
    def apply[V](kvStore: KVStore[V]) = kvStore match {
      case Put(k, v) => State.modify[MapKV]{ map =>
        map.updated(k, v)
      }
      case Get(k)    => State.inspect[MapKV, V]{ map =>
        map.get(k).asInstanceOf[V]
      }
      case Delete(k) => State[MapKV, V]{ map =>
        val existed = map.get(k).isDefined
        (map - k, existed)
      }
    }
  }

  /* ************************** */
  /* COMPOSING FREE MONADS ADTs */
  /* ************************** */
  sealed trait Interact[A]
  final case class Ask(prompt: String) extends Interact[String]
  final case class Tell(msg: String) extends Interact[Unit]

  sealed trait DataOp[A]
  final case class AddCat(cat: String) extends DataOp[Unit]
  final case object GetAllCats extends DataOp[List[String]]

  import cats.data.EitherK
  type CatsApp[A] = EitherK[DataOp, Interact, A]

  import cats.InjectK
  object Interact {
    def ask[F[_]](prompt: String)(implicit I: InjectK[Interact, F]): Free[F, String] =
      Free.inject(Ask(prompt))
    def tell[F[_]](msg: String)(implicit I: InjectK[Interact, F]): Free[F, Unit] =
      Free.inject(Tell(msg))
  }
  object DataOp {
    def addCat[F[_]](cat: String)(implicit I: InjectK[DataOp, F]): Free[F, Unit] =
      Free.inject(AddCat(cat))
    def getAllCats[F[_]](implicit I: InjectK[DataOp, F]): Free[F, List[String]] =
      Free.inject(GetAllCats)
  }

  def programKitties(implicit I1: InjectK[Interact, CatsApp], I2: InjectK[DataOp, CatsApp]): Free[CatsApp, Unit] = {
    import Interact._
    import DataOp._
    for {
      cat <- ask("What's the kitty's name?")
      _ <- addCat(cat)
      cats <- getAllCats
      _ <- tell(cats.toString)
    } yield ()
  }

  val interactConsoleInterpreter: (Interact ~> Id) = new (Interact ~> Id) {
    def apply[A](i: Interact[A]): Id[A] = i match {
      case Ask(prompt) =>
        println(prompt)
        scala.io.StdIn.readLine()
      case Tell(msg) =>
        println(msg)
    }
  }

  val dataOpInMemoryInterpreter: (DataOp ~> Id) = new (DataOp ~> Id) {
    val cats = scala.collection.mutable.ListBuffer.empty[String]
    def apply[A](dataOp: DataOp[A]): Id[A] = dataOp match {
      case AddCat(cat) =>
        cats += cat
        ()
      case GetAllCats  => cats.toList
    }
  }

  val catsAppInterpreter: (CatsApp ~> Id) = dataOpInMemoryInterpreter or interactConsoleInterpreter



  /* ********** */
  /* EXECUTIONS */
  /* ********** */
  println(program.foldMap(impureCompiler))
  println(program.foldMap(pureCompiler).run(Map.empty).value)
  programKitties.foldMap(catsAppInterpreter)
}
