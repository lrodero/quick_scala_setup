package codility.lesson6

object Distinct extends App {

  object Solution {
    def solution(a: Array[Int]): Int = {
      if(a.size <= 1) a.size
      val sorted = a.sortBy(i => i)
      println(sorted.mkString("[",",","]"))
      var distinct = 1
      for(i <- 1 until a.size) {
        if(sorted(i) != sorted(i-1)) distinct += 1
      }
      distinct
    }
  }

  println(Solution.solution(Array(2,1,1,4,3,1)))
  println(Solution.solution(Array(2,2,2,3,3,7)))
  println(Solution.solution(Array(2,1)))
  println(Solution.solution(Array(1)))

}

