package shapelezz

import scalaz.scalacheck.ScalazProperties._
import org.scalacheck.ScalacheckShapeless._

class SumTest extends Spec {

  import scalaz.std.anyVal._
  import scalaz.std.string._

  sealed trait Cases[A, B]
  case class Case1[A, B](a: A) extends Cases[A, B]
  case class Case2[A, B](b: B) extends Cases[A, B]

  checkAll("cases", order.laws[Cases[String, Int]])

}
