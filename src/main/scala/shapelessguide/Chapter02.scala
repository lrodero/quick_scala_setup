package shapelessguide

import shapeless.Generic // to convert from<->to ADTs
import shapeless.{HList, ::, HNil} // Imports for products
import shapeless.{Coproduct, :+:, CNil, Inl, Inr} // Imports for coproducts

object Chapter02 extends App {

  
  case class IceCream(name: String, numCherries: Int, inCone: Boolean)
  case class Employee(name: String, age: Int, isManager: Boolean)
  val iceCreamGen = Generic[IceCream]
  val employeeGen = Generic[Employee]
  val emp: Employee = employeeGen.from(iceCreamGen.to(IceCream("Ice",60,true)))
  
  val product: String :: Int :: Boolean :: HNil =
      "Sunday" :: 1 :: false :: HNil


  val product2: HList = product

  case class Red()
  case class Ambar()
  case class Green()
  type Light = Red :+: Ambar :+: Green :+: CNil

  val red: Light = Inl(Red())
  val red2: Light = Inr(Inl(Ambar()))

  sealed trait Shape
  final case class Rectangle(width: Double, height: Double) extends Shape
  final case class Circle(radius: Double) extends Shape

  // val gen = Generic[Shape] doesn't work, even if it is in the manual

}

/******************************************************************/
// DERIVING TYPE CLASSES FOR PRODUCTS (I.E. CASE CLASSES, TUPLES) //
/******************************************************************/
object Chapter03 {

  trait CsvEncoder[A] {
    def encode(value: A): List[String]
  }

  object CsvEncoder {
    
    def apply[A](implicit csvEnc: CsvEncoder[A]): CsvEncoder[A] =
      csvEnc
    
    def instance[A](func: A => List[String]): CsvEncoder[A] =
      new CsvEncoder[A] {
        override def encode(a: A): List[String] =
          func(a)
      }

    implicit def pairEncoder[A, B](implicit encA: CsvEncoder[A], encB: CsvEncoder[B]): CsvEncoder[(A,B)] = new CsvEncoder[(A,B)] {
      override def encode(ab: (A,B)): List[String] = {
        val (a,b) = ab
        encA.encode(a) ::: encB.encode(b)
      }
    }

    implicit val stringEncoder: CsvEncoder[String] = instance { s =>
      s :: Nil
    }

    implicit val intEncoder: CsvEncoder[Int] = instance { i =>
      i.toString :: Nil
    }

    implicit val boolEncoder: CsvEncoder[Boolean] = instance { b =>
      (if(b) "yes" else "no") :: Nil
    }

    implicit val hnilEncoder: CsvEncoder[HNil] = instance { HNil =>
      Nil
    }

    implicit def hlistEncoder[H, T <: HList](
      implicit
      hEncoder: CsvEncoder[H],
      tEncoder: CsvEncoder[T]): CsvEncoder[H :: T] = instance { case h :: t =>
      hEncoder.encode(h) ::: tEncoder.encode(t)
    }

  }

  case class Employee(name: String, age: Int, isManager: Boolean)
  implicit val empCsvEncoder = CsvEncoder.instance{ emp: Employee =>
      emp.name :: emp.age.toString :: emp.isManager.toString :: Nil
    }

  import CsvEncoder._
  case class IceCream(name: String, numCherries: Int, inCone: Boolean)
  implicit val iceCreamCsvEncoder: CsvEncoder[IceCream] = {
    val gen = Generic[IceCream]
    val enc = CsvEncoder[gen.Repr]
    CsvEncoder.instance(ic => enc.encode(gen.to(ic)))
  }

  def writeCsv[A](as: List[A])(implicit encoder: CsvEncoder[A]) =
    as.map(a => encoder.encode(a).mkString(",")).mkString("\n")


}
