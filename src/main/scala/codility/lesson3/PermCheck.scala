package codility.lesson3

object PermCheck extends App {

  def translate(arr: Array[Int], arr2: Array[Int]): Int = {
    for(i <- 0 until arr2.length) {
      if((arr(i) < 1) || (arr(i) > arr.length)) 
        return 0
      else
        arr2(arr(i)-1) = 1
    }
    1
  }

  def isPerm(arr:Array[Int]): Int = {
    val arr2: Array[Int] = Array.fill((arr.length)){0}
    val res = translate(arr,arr2)

    if(res == 0)
      0
    else if((arr2.find(_ == 0).isDefined))
      0
    else
      1
  }

  val arr: Array[Int] = Array(1,2,3,4,5)
  println(s"Is perm: ${isPerm(arr)}")
  val arr2: Array[Int] = Array(1,2,3,5,6)
  println(s"Is perm: ${isPerm(arr2)}")

}
