package shapelessguide

import shapeless.{HList, ::, HNil}

object Chapter02 extends App {
  val product: String :: Int :: Boolean :: HNil =
      "Sunday" :: 1 :: false :: HNil
}
