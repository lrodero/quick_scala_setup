package codility.lesson9

object MaxSlice extends App {
  def maxSlice(arr: Array[Int]): (Int,Int,Int) = {
    var maxEnding = 0
    var maxSlice = 0
    var endingInd = -1
    for(i <- 0 until arr.size) {
      val a = arr(i)
      maxEnding = math.max(0, maxEnding + a)
      if(maxEnding > maxSlice) {
        endingInd = i
        maxSlice = maxEnding
      }
    }
    var acc = 0
    var j = endingInd
    while(acc != maxSlice) {
       acc += arr(j)
       j -= 1
    }
    j += 1
    (maxSlice,j,endingInd)
  }

  println(maxSlice(Array(5,-7,3,5,-2,4,-1)))

}
