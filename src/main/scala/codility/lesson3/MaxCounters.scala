package codility.lesson3

object MaxCounters extends App {

  def increase(counters: Array[Int], i: Int): Unit =
    counters(i-1) += 1

  def maxCounter(counters: Array[Int]): Unit = {
    val max = counters.max
    counters.map(_ => max)
  }

  val arr: Array[Int] = Array(3,4,4,6,1,4,4)

  def solution(n: Int, a: Array[Int]): Array[Int] = {

    val counters: Array[Int] = Array.fill(n){0}

    var max: Int = 0
    var reset: (Int, Int) = (-1,0) // when, max
    for(index <- 0 until a.size) {
      val i = a(index)
      if(i < n) {
        counters(i-1) += 1
        max = math.max(counters(i-1), max)
      } else {
        reset = (index, max)
      }
    }

    if(reset._1 >= 0) {
      for(i <- 0 until counters.size) {
        counters(i) = reset._2
      }
      for(index <- reset._1+1 until a.size) {
        val i = a(index)
        counters(i-1) += 1
      }
    }

    counters
  }

  println(solution(5, arr).mkString("[",",","]"))

}
