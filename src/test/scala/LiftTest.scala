package shapelezz

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop.forAll
import scalaz._
import scalaz.std.option._
import scalaz.std.string._
import scalaz.syntax.applicative._

class LiftTest extends Spec {

  def foo(x: Int, y: String, z: Float) = s"$x - $y - $z"
  val lifted = Applicative[Option].liftA(foo _)

  // check for type
  val _: (Option[Int], Option[String], Option[Float]) => Option[String] = lifted

  "lifting a ternary operation" ! forAll { (x: Option[Int], y: Option[String], z: Option[Float]) =>
    val r1 = lifted(x, y, z)
    val r2 = Apply[Option].ap3(x, y, z)((foo _).pure[Option])
    r1 must_=== r2
  }

}
