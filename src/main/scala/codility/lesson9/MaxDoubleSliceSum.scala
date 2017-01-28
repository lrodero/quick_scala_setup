package codility.lesson9

object MaxDoubleSliceSum extends App {

  object Solution {
    def solution(a: Array[Int]): Int = {

      val max: (Int, Int, Int) = MaxSlice.maxSlice(a)
      println(max)

      var min = Int.MaxValue
      var minPos = -1
      for(i <- max._2 to max._3) {
        if(min > a(i)) {
          minPos = 1
          min = a(i)
        }
      }
      println(s"$min $minPos")

      a.slice(max._1,minPos).sum +
      a.slice(minPos,max._3+1).sum
    }
  }

  println(Solution.solution(Array(3,2,6,-1,4,5,-1,2)))

}
