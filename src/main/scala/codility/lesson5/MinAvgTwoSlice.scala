package codility.lesson5

object MinAvgTwoSlice extends App {

  object Solution {
    def solution(a: Array[Int]): Int = {
      val prefixAcc = Array.fill(a.size){0}
      val suffixAcc = Array.fill(a.size){0}
      prefixAcc(0) = a(0)
      for(i <- 1 until a.size) {
        prefixAcc(i) = prefixAcc(i-1) + a(i)
      }
      suffixAcc(a.size-1) = a.last
      for(i <- a.size-2 to 0 by -1) {
        suffixAcc(i) = suffixAcc(i+1) + a(i)
      }
      println(prefixAcc.mkString("[",",","]"))
      println(suffixAcc.mkString("[",",","]"))
      var min = (Double.MaxValue, 0)
      for(left  <- 0      until a.size-1;
          right <- left+1 until a.size) {
        val avg: Double =
          if(left > 0)
            (prefixAcc(right) - prefixAcc(left-1)) / (right-left+1).toDouble
          else 
            (prefixAcc(right)) / (right-left+1).toDouble
        if(avg < min._1)
          min = (avg, left)
      }
      min._2
    }
  }

  println(Solution.solution(Array(4,2,2,5,1,5,8)))

}

