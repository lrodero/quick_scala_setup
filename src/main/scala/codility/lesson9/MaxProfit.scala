package codility.lesson9

object MaxProfit extends App {

  // MaxSlice.maxSlice(a:Array[Int]): (Int,Int,Int)
  object Solution {
    def solution(a: Array[Int]): Int = {
      val diffs = Array.fill(a.size){0}
      for(i <- 1 until a.size) {
        diffs(i) = a(i) - a(i-1)
      }
     MaxSlice.maxSlice(diffs)._1
    }
  }

  println(Solution.solution(Array(23171,21011,21123,21366,21013,21367)))

}
