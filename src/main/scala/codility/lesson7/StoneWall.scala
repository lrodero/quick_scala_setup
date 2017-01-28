package codility.lesson7

object StoneWall extends App {

  object Solution {
    def solution(h: Array[Int]): Int = {
      var prevHeight = -1
      var blocks = 0
      for(i <- 0 until h.size) {
        val height = h(i)
        if(height > prevHeight) {
          blocks += 1
        } else if(height < prevHeight) {
          var j = i -1
          while(j >= 0 && h(j) > height) {
            j -= 1
          }
          if(j < 0 || h(j) < height) {
            blocks += 1
          }
        }
        prevHeight= height
      }
      blocks
    }
    def solution2(h: Array[Int]): Int = {
      val heights = new scala.collection.mutable.Stack[Int]
      var blocks = 0
      for(i <- 0 until h.size) {
        val height = h(i)
        while(!(heights.isEmpty) && (heights.top > height) ) {
          heights.pop()
        }
        if(heights.isEmpty || (heights.top < height) ) {
          blocks += 1
        }
        heights push height
      }
      blocks
    }
  }

  println(Solution.solution(Array(1,2,3)))
  println(Solution.solution(Array(8,8,5,7,9,8,7,4,8)))
  println(Solution.solution2(Array(1,2,3)))
  println(Solution.solution2(Array(8,8,5,7,9,8,7,4,8)))

}
