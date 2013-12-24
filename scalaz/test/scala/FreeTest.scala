package shapeless.contrib.scalaz

import org.specs2.scalaz.Spec
import scalaz.scalacheck.ScalazArbitrary._
import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalaCheckBinding._
import scalaz._
import scalaz.std.AllInstances._
import shapeless.contrib.scalaz.free._
import org.scalacheck.{Arbitrary, Gen}

class FreeTest extends Spec {

  // TODO Gosub
  implicit def freeArbitrary[F[+_]: Functor, A](implicit
    A: Arbitrary[A],
    F0: shapeless.Lazy[Arbitrary[F[Free[F, A]]]]
  ): Arbitrary[Free[F, A]] =
    Arbitrary(Gen.frequency(
      (1, Functor[Arbitrary].map(A)(Free.Return[F, A](_)).arbitrary),
      (1, Functor[Arbitrary].map(F0.value)(Free.Suspend[F, A](_)).arbitrary)
    ))

  checkAll(order.laws[Free[Option, Int]])

  "Free[Option, Int] shows" ! prop{ a: Free[Option, Int] =>
    Show[Free[Option, Int]].shows(a) must_== a.toString
  }

}

