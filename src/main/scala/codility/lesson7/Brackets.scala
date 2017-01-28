package codility.lesson7

object Brackets extends App {

  object Solution {
    def solution(s: String): Int = {
      val brackets = s.toCharArray
      val stack = new scala.collection.mutable.Stack[Char]()
      for(b <- brackets) {
        println(s"$b")
        b match {
          case '{' | '[' | '(' => stack push b
          case '}' if (stack.isEmpty || stack.pop() != '{') => return 0
          case ']' if (stack.isEmpty || stack.pop() != '[') => return 0
          case ')' if (stack.isEmpty || stack.pop() != '(') => return 0
          case _ => () // Continue
        }
      }
      if (stack.isEmpty)
        1
      else
        0
    }
  }

  println(Solution.solution("{[()()]}"))
  println(Solution.solution("([)()]"))

}
