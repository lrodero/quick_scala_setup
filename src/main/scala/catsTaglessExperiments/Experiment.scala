package catsTaglessExperiments

import cats.tagless._

import scala.util.Try

@finalAlg
@autoFunctorK
@autoSemigroupalK
@autoProductNK
trait ExpressionAlg[F[_]] {
  def num(i: String): F[Float]
  def divide(dividend: Float, divisor: Float): F[Float]
}

object ExpressionAlgInstances {
  implicit object tryExpression extends ExpressionAlg[Try] {
    def num(i: String): Try[Float] = Try(i.toFloat)
    def divide(dividend: Float, divisor: Float): Try[Float] = Try(dividend/divisor)
  }
}

object Main extends App {
  println("Now to start working...")
}
