package codility.lesson5

object PassingCars extends App {

  object Solution {
    def solution(a: Array[Int]): Int = {
      val accs = Array.fill(a.size){0}
      var acc = 0
      for(i <- 0 until a.size) {
        acc += a(i)
        accs(i) = acc
      }
      var passings = 0
      for(i <- 0 until a.size) {
        if(a(i) == 0)
          passings += (accs(accs.length-1) - accs(i))
      }
      passings
    }
    def solution2(a: Array[Int]): Int = {
      var zerosSoFar = 0
      var acc = 0
      for(i <- 0 until a.size) {
        if(a(i) == 0)
          zerosSoFar += 1
        else
          acc += zerosSoFar
      }
      acc
    }
  }

  println(Solution.solution(Array(0,1,0,1,1)))
  println(Solution.solution2(Array(0,1,0,1,1)))

}

