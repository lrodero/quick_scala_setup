package codility.lesson7

object Fish extends App {

  object Solution {
    def solution(a: Array[Int], b: Array[Int]): Int = {
      var alive = 0
      val upstreams = new scala.collection.mutable.Stack[Int]()
      for(i <- 0 until b.size) {
        if(b(i) == 0) { // downstream
          while(!upstreams.isEmpty && upstreams.top < a(i)) {
            upstreams.pop()
          }
          if(upstreams.isEmpty)
            alive += 1
        } else { // upstream
          upstreams push a(i)
        }
      }
      alive + upstreams.size
    }
  }

  println(Solution.solution(Array(4,3,2,1,5), Array(0,1,0,0,0)))

}
