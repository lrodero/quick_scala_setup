package codility.lesson5

object GenomicRangeQuery extends App {

  object Solution {
    def solution(s: String, p: Array[Int], q: Array[Int]): Array[Int] = {

      def weight(c:Char): Int = c match {
        case 'A' => 1
        case 'C' => 2
        case 'G' => 3
        case 'T' => 4
      }

      val dna = s.toCharArray
      if(dna.size == 0) return Array.empty[Int]

      val sumPrefixCs = Array.fill(dna.size){0}
      val sumPrefixGs = Array.fill(dna.size){0}
      val sumPrefixTs = Array.fill(dna.size){0}
      val sumPrefixAs = Array.fill(dna.size){0}

      dna(0) match {
        case 'C' => sumPrefixCs(0) = 1
        case 'G' => sumPrefixGs(0) = 1
        case 'T' => sumPrefixTs(0) = 1
        case 'A' => sumPrefixAs(0) = 1
      }
      for(i <- 1 until dna.size) {
        dna(i) match {
          case 'C' => sumPrefixCs(i) = sumPrefixCs(i-1) + 1
          case 'G' => sumPrefixGs(i) = sumPrefixGs(i-1) + 1
          case 'T' => sumPrefixTs(i) = sumPrefixTs(i-1) + 1
          case 'A' => sumPrefixAs(i) = sumPrefixAs(i-1) + 1
        }
      }

      val result = Array.fill(p.size){0}
      for(i <- 0 until p.size) {
        val left = p(i)
        val right = q(i)
        if(left == right)
          result(i) = weight(dna(left))
        else if(sumPrefixCs(right)-sumPrefixCs(left) > 0)
          result(i) = weight('C')
        else if(sumPrefixGs(right)-sumPrefixGs(left) > 0)
          result(i) = weight('G')
        else if(sumPrefixTs(right)-sumPrefixTs(left) > 0)
          result(i) = weight('T')
        else if(sumPrefixAs(right)-sumPrefixAs(left) > 0)
          result(i) = weight('A')
        else throw new Error("Ups")
      }

      result
      
    }
  }

  println(Solution.solution("CAGCCTA", Array(2,5,0), Array(4,5,6)).mkString("[",",","]"))


}

