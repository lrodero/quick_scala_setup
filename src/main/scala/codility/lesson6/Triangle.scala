package codility.lesson6

object Triangle extends App {

  object Solution {
    def solution(a: Array[Int]): Int = {

      def isTriangular(i1: Int, i2: Int, i3: Int): Boolean = {
        (i1 + i2 > i3) &&
        (i1 + i3 > i2) &&
        (i2 + i3 > i1)
      }

      if(a.size < 3)
        return 0

      // Brute force
      //for(i <- 0     until a.size - 2;
      //    j <- i + 1 until a.size - 1;
      //    k <- i + 2 until a.size) {
      //  if(isTriangular(a(i), a(j), a(k)))
      //    return 1
      //}

      val sorted = a.sorted
      println(sorted.mkString("[",",","]"))
      //var j = 1
      //var k = 2
      //var i = 0
      //while(i < a.size - 2) {
      //  var j = i + 1
      //  while(j < a.size - 1) {
      //    var k = j + 1
      //    while(k < a.size) {
      //      if(sorted(i) + sorted(j) <= sorted(k))
      //        k = a.size
      //      else if(isTriangular(sorted(i), sorted(j), sorted(k)))
      //        return 1
      //      else
      //        k += 1
      //    }
      //    j += 1
      //  }
      //  i += 1
      //}

      for(i <- 0 until a.size - 2) {
        if(isTriangular(sorted(i),sorted(i+1),sorted(i+2)))
          return 1
      }

      return 0
    }
  }

  println(Solution.solution(Array(10,2,5,1,8,20)))
  println(Solution.solution(Array(10,50,5,1)))
}

