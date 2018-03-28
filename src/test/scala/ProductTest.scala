package shapelezz

import scalaz.scalacheck.ScalazProperties._
import org.scalacheck.ScalacheckShapeless._

class ProductTest extends Spec {

  import scalaz.std.anyVal._
  import scalaz.std.string._

  case class OneElem(n: Int)

  checkAll("one element", order.laws[OneElem])
  checkAll("one element", monoid.laws[OneElem])

  case class TwoElem(n: Int, x: String)

  checkAll("two elements", order.laws[TwoElem])
  checkAll("two elements", monoid.laws[TwoElem])

}
