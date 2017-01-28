package codility.lesson12

object BinarySearch extends App {
  def binarySearch(a: Array[Int], item: Int): Int = {
    @annotation.tailrec
    def search(a: Array[Int], item: Int, from: Int, to: Int): Int = {
      if(item < a(from) || item > a(to-1))
        return -1

      if(from == to) {
        if(a(from) == item)
          return from
        else
          return -1
      }

      val middle = from + (to-from)/2
      val middleVal = a(middle)
      if(middleVal == item)
        return middle

      if(middleVal > item)
        search(a, item, from, middle)
      else
        search(a, item, middle+1, to)

    }
    if(a.size == 0) 
      -1
    else
      search(a, item, 0, a.size)
  }

  println(binarySearch(Array(0,1,2,3),3))
}
