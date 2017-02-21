/**AKKA SAMPLE MAIN example as defined at
 * http://www.lightbend.com/activator/template/akka-sample-main-scala
 */
package sample.hello

object Main {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[HelloWorld].getName))
  }
}
