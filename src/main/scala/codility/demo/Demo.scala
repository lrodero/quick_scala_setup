package codility.demo

import scala.collection.JavaConverters._

object Demo extends App {
  object Solution {
    def solution(a: Array[Int]): Int = {
      val accsPrefix: Array[Int] = Array.fill(a.size){0}
      val accsSuffix: Array[Int] = Array.fill(a.size){0}

      accsPrefix(0) = a(0)
      accsSuffix(a.size-1) = a.last
      for(i <- 1 until a.size) {
        accsPrefix(i) = accsPrefix(i-1) + a(i)
        accsSuffix(a.size - (i + 1)) = a(a.size - (i + 1)) + accsSuffix(a.size - i)
      }

      println(accsPrefix.mkString("[",",","]"))
      println(accsSuffix.mkString("[",",","]"))

      // P == 0
      if(accsSuffix(1) == 0) return 0
      // P == N-1
      if(accsPrefix(a.size - 2) == 0) return a.size - 1

      for(i <- 1 until (a.size - 1)) {
        println(s"$i ${accsPrefix(i-1)} ${accsSuffix(i + 1)}")
        if(accsPrefix(i - 1) == accsSuffix(i + 1))
          return i
      }

      return -1

    }
  }
  println(Solution.solution(Array(-1,3,-4,5,1,-6,2,1)))
  println(Solution.solution(Array(-1,1,1)))
  println(Solution.solution(Array(4,-1,-1,2,2)))
}
