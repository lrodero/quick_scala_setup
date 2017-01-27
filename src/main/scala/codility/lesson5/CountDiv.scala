package codility.lesson5

object CountDiv extends App {

  object Solution {
    def solution(a: Int, b: Int, k: Int): Int = {
      (b/k)-((a-1)/k)
    }
  }

  println(Solution.solution(33,42,5))
  println(Solution.solution(6,11,2))
  println(Solution.solution(5,15,5))
  println(Solution.solution(6,12,4))
  println(Solution.solution(6,14,7))
  println(Solution.solution(2,5,10))
  println(Solution.solution(3,12,5))

}
