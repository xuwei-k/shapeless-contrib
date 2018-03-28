package shapelezz

import scalaz.scalacheck.ScalazArbitrary._
import scalaz.scalacheck.ScalazProperties.order
import scalaz.scalacheck.ScalaCheckBinding._
import scalaz._
import scalaz.Isomorphism._
import scalaz.std.AllInstances._
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class CofreeTest extends Spec {

  type CofreeList[A] = Cofree[List, A]

  val treeCofreeListIso: Tree <~> CofreeList =
    new IsoFunctorTemplate[Tree, CofreeList] {
      def to[A](tree: Tree[A]) =
        Cofree(tree.rootLabel, tree.subForest.map(to).toList)
      def from[A](c: CofreeList[A]) =
        Tree.node(c.head, c.tail.map(from(_)).toStream)
    }

  implicit class CofreeOps[A](self: Cofree[Maybe, A]){
    def toList: List[A] =
      self.head :: self.tail.map(_.toList).getOrElse(Nil)
  }

  type OneAndList[A] = OneAnd[List, A]
  type CofreeMaybe[A] = Cofree[Maybe, A]

  val oneAndListToCofreeMaybe: OneAndList ~> CofreeMaybe =
    new (OneAndList ~> CofreeMaybe) {
      override def apply[A](fa: OneAndList[A]) =
        Cofree.unfold(fa) {
          case OneAnd(a, h :: t) =>
            (a, Maybe.just(OneAnd(h, t)))
          case OneAnd(a, _) =>
            (a, Maybe.empty)
        }
    }

  implicit def CofreeMaybeArb[A: Arbitrary]: Arbitrary[CofreeMaybe[A]] = {
    import org.scalacheck.Arbitrary._
    import org.scalacheck.Gen
    val arb = Arbitrary { Gen.listOfN(20, implicitly[Arbitrary[A]].arbitrary ) }
    Functor[Arbitrary].map(arb){
      case h :: Nil => oneAndListToCofreeMaybe(OneAnd(h, Nil))
      case h :: t => oneAndListToCofreeMaybe(OneAnd(h, t))
    }
  }

  implicit def CofreeListArb[A: Arbitrary]: Arbitrary[CofreeList[A]] =
    Functor[Arbitrary].map(implicitly[Arbitrary[Tree[A]]])(treeCofreeListIso.to)

  implicit def treeShow[A: Show]: Show[Tree[A]] = Show.showA

  //checkAll(order.laws[Cofree[Option, Int]])
  //checkAll(order.laws[Cofree[List, Int]])

  /*"treeCofreeListIso" ! prop{ (a: Tree[Int], b: Cofree[List, Int]) =>
    treeCofreeListIso.from(treeCofreeListIso.to(a)) must equal(a)
    treeCofreeListIso.to(treeCofreeListIso.from(b)) must equal(b)
  }*/

  "Equal[Cofree[List, Int]] is Equal[Tree[Int]]" ! forAll { (a: Cofree[List, Int], b: Cofree[List, Int]) =>
    Equal[Cofree[List, Int]].equal(a, b) must_== Equal[Tree[Int]].equal(treeCofreeListIso.from(a), treeCofreeListIso.from(b))
  }

  "Order[Cofree[Maybe, Int]] is Order[List[Int]]" ! forAll { (a: Cofree[Maybe, Int], b: Cofree[Maybe, Int]) =>
    val aa = a.toList
    val bb = b.toList
    Equal[Cofree[Maybe, Int]].equal(a, b) must_== Equal[List[Int]].equal(aa, bb)
    Order[Cofree[Maybe, Int]].order(a, b) must_== Order[List[Int]].order(aa, bb)
  }

}

