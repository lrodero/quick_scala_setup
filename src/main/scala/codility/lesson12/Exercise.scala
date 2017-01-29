package codility.lesson12

// BinarySearch.binarySearch(a: Array[Int], item: Int)
object Exercise extends App {
  object Solution {
    def solution(k: Int, a: Array[Int]): Int = {
      // Bin search
      var beg = 0
      var end = a.size
      var result = -1
      while(beg <= end) {
        var mid = (end - beg) / 2
        if(check(a, mid) <= k) {
          end = mid - 1
          result = mid
        } else {
          beg = mid + 1
        }
      }
      result
    }

    def check(a: Array[Int], size: Int): Int = {
      var boards = 0
      var i = 0
      while(i < a.size) {
        if(a(i) == 1) {
          boards += 1
          i += size
        } else {
          i += 1
        }
      }
      boards
    }
  }

  println(Solution.solution(5, Array(2,1,5,1,2,2,2)))
}
