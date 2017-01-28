package codility.lesson7

object Nesting extends App {

  object Solution {
    def solution(s: String): Int = {
      var count = 0
      for(c <- s.toCharArray) {
        c match {
          case '(' => count += 1
          case ')' => count -=1
        }
        if(count < 0)
          return 0
      }
      if(count == 0)
        1
      else
        0
    }
  }

  println(Solution.solution("(()(())())"))
  println(Solution.solution("())"))

}
