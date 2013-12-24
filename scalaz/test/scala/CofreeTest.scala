package shapeless.contrib.scalaz

import org.specs2.scalaz.Spec
import scalaz.scalacheck.ScalazArbitrary._
import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalaCheckBinding._
import scalaz._
import scalaz.Isomorphism._
import scalaz.std.AllInstances._
import org.scalacheck.{Arbitrary, Gen}

class CofreeTest extends Spec {

  type CofreeList[A] = Cofree[List, A]

  val treeCofreeListIso: Tree <~> CofreeList =
    new IsoFunctorTemplate[Tree, CofreeList] {
      def to[A](tree: Tree[A]): CofreeList[A] =
        Cofree(tree.rootLabel, tree.subForest.map(to).toList)
      def from[A](c: CofreeList[A]): Tree[A] =
        Tree.node(c.head, c.tail.map(from(_)).toStream)
    }

  implicit def CofreeListArb[A: Arbitrary]: Arbitrary[CofreeList[A]] =
    Functor[Arbitrary].map(implicitly[Arbitrary[Tree[A]]])(treeCofreeListIso.to)

  checkAll(order.laws[Cofree[List, Int]])

  "Cofree[List, Int] shows" ! {
    Show[Cofree[List, Int]].shows(Cofree(1, List(Cofree(2, List(Cofree(3, Nil))), Cofree(4, Nil)))) must_==
      """Cofree(1, [Cofree(2, [Cofree(3, [])]),Cofree(4, [])])"""
  }

}

