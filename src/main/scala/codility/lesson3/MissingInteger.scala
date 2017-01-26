package codility.lesson3

object MissingInteger extends App {

  val arr = Array(1,3,6,4,1,2)

  def solution(a: Array[Int]): Int = {

    val min = a.filter(_ > 0).min
    if(min > 1)
      min - 1
    else {
      val b = Array.fill(a.length){0}
      for(index <- 0 until a.size
          if((a(index) > 0) && (a(index) <= b.size))
         ) {
        b(a(index)-1) = 1
      }
      for(index <- 0 until b.size if(b(index) == 0)) {
        return index+1
      }
      return -1
    }
  }

  println(solution(arr))

}
