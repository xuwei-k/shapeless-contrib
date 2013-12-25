package shapeless.contrib.scalaz

import org.specs2.scalaz.{Spec, ScalazMatchers}
import scalaz.scalacheck.ScalazArbitrary._
import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalaCheckBinding._
import scalaz._
import scalaz.Isomorphism._
import scalaz.std.AllInstances._
import org.scalacheck.{Arbitrary, Gen}

class CofreeTest extends Spec with ScalazMatchers{

  type CofreeList[A] = Cofree[List, A]

  val treeCofreeListIso: Tree <~> CofreeList =
    new IsoFunctorTemplate[Tree, CofreeList] {
      def to[A](tree: Tree[A]): CofreeList[A] =
        Cofree(tree.rootLabel, tree.subForest.map(to).toList)
      def from[A](c: CofreeList[A]): Tree[A] =
        Tree.node(c.head, c.tail.map(from(_)).toStream)
    }

  def cofreeOpt2List[A](fa: Cofree[Option, A]): List[A] =
    fa.head :: fa.tail.map(cofreeOpt2List).getOrElse(Nil)

  implicit def CofreeArb[F[+_]: Functor, A](implicit A: Arbitrary[A], F: shapeless.Lazy[Arbitrary[F[Cofree[F, A]]]]): Arbitrary[Cofree[F, A]] =
    Apply[Arbitrary].apply2(A, F.value)(Cofree(_, _))

  implicit def CofreeListArb[A: Arbitrary]: Arbitrary[CofreeList[A]] =
    Functor[Arbitrary].map(implicitly[Arbitrary[Tree[A]]])(treeCofreeListIso.to)

  implicit def treeShow[A: Show]: Show[Tree[A]] = Show.showA

  checkAll(order.laws[Cofree[Option, Int]])
  checkAll(order.laws[Cofree[List, Int]])

  "treeCofreeListIso" ! prop{ (a: Tree[Int], b: Cofree[List, Int]) =>
    treeCofreeListIso.from(treeCofreeListIso.to(a)) must equal(a)
    treeCofreeListIso.to(treeCofreeListIso.from(b)) must equal(b)
  }

  "Equal[Cofree[List, Int]] is Equal[Tree[Int]]" ! prop{ (a: Cofree[List, Int], b: Cofree[List, Int]) =>
    Equal[Cofree[List, Int]].equal(a, b) must_== Equal[Tree[Int]].equal(treeCofreeListIso.from(a), treeCofreeListIso.from(b))
  }

  "Order[Cofree[Option, Int]] is Order[List[Int]]" ! prop{ (a: Cofree[Option, Int], b: Cofree[Option, Int]) =>
    val aa = cofreeOpt2List(a)
    val bb = cofreeOpt2List(b)
    Equal[Cofree[Option, Int]].equal(a, b) must_== Equal[List[Int]].equal(aa, bb)
    Order[Cofree[Option, Int]].order(a, b) must_== Order[List[Int]].order(aa, bb)
  }

  "Cofree[List, Int] shows" ! {
    Show[Cofree[List, Int]].shows(Cofree(1, List(Cofree(2, List(Cofree(3, Nil))), Cofree(4, Nil)))) must_==
      """Cofree(1, [Cofree(2, [Cofree(3, [])]),Cofree(4, [])])"""
  }

}

