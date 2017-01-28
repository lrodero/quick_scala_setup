package codility.lesson9

object MaxSliceSum extends App {

  // MaxSlice.maxSlice(a:Array[Int]): (Int,Int,Int)
  object Solution {
    def solution(a: Array[Int]): Int = {
      MaxSlice.maxSlice(a)._1
    }
  }

  println(Solution.solution(Array(3,2,-6,4,0)))

}
