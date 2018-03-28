package shapelezz

import scalaz._
import scalaz.scalacheck.ScalazProperties.order
import scalaz.scalacheck.ScalaCheckBinding._
import scalaz.std.AllInstances._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.forAll

class FreeTest extends Spec {

  implicit def freeArbitrary[F[_]: Functor, A](implicit
    A: Arbitrary[A],
    F0: shapeless.Lazy[Arbitrary[F[Free[F, A]]]]
  ): Arbitrary[Free[F, A]] =
    Arbitrary(Gen.oneOf(
      Functor[Arbitrary].map(A)(Free.point[F, A](_)).arbitrary,
      Functor[Arbitrary].map(F0.value)(Free.roll[F, A](_)).arbitrary
    ))

  type PairOpt[A] = Option[(A, A)]
  type FList[A] = Free[PairOpt, A] // Free Monad List

  implicit val pairOptFunctor: Functor[PairOpt] =
    new Functor[PairOpt]{
      def map[A, B](fa: PairOpt[A])(f: A => B) =
        fa.map{ t => (f(t._1), f(t._2)) }
    }

  implicit class ListOps[A](self: List[A]){
    def toFList: FList[A] = self match {
      case h :: t =>
        Free.roll[PairOpt, A](Option((Free.point[PairOpt, A](h), t.toFList)))
      case Nil =>
        Free.liftF[PairOpt, A](None)
    }
  }

  checkAll(order.laws[FList[Int]])

  "Order[List[Int]] is Order[FList[Int]]" ! forAll { (a: List[Int], b: List[Int]) =>
    val aa = a.toFList
    val bb = b.toFList
    Equal[List[Int]].equal(a, b) must_== Equal[FList[Int]].equal(aa, bb)
    Order[List[Int]].order(a, b) must_== Order[FList[Int]].order(aa, bb)
  }

  "shows" ! forAll { a: FList[Int] =>
    Show[FList[Int]].shows(a) must_== a.toString
  }

}

