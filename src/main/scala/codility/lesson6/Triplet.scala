package codility.lesson6

object Triplet extends App {

  object Solution {
    def solution(a: Array[Int]): Int = {
      val sorted = a.sortBy(i => i)
      sorted.last * sorted.init.last * sorted.init.init.last
    }
  }

  println(Solution.solution(Array(1,2,3,4)))
}

