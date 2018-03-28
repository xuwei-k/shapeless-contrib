package  shapelezz

import scalaz.{Equal, \/}
import scalaz.std.AllInstances._
import scalaprops._
import scalaprops.Property.forAll
import scalaprops.ScalapropsMagnolia._

object BinaryTest extends Scalaprops {
  private[this] implicit val stringGen: Gen[String] = Gen.asciiString

  def binaryLaws[A : Binary : Equal : Gen] =
    forAll { (a: A, rest: Vector[Byte]) =>
      val encoded = Binary[A] encode a
      val decoded = Binary[A] decode (encoded ++ rest)
      Equal[Option[(A, Vector[Byte])]].equal(decoded, Some((a, rest)))
    }.toProperties(())

  val int = binaryLaws[Int]
  val `(Int, Int)` = binaryLaws[(Int, Int)]
  val `Int \\/ Long` = binaryLaws[Int \/ Long]
  val `List[Int]` = binaryLaws[List[Int]]
  val `String` = binaryLaws[String]

  case class OneElem(n: Int)
  case class TwoElem(n: Int, x: String)
  case class Complex(n: Int, x: TwoElem \/ String, z: List[OneElem])

  val complex = {
    import Binary.auto._
    Properties.list(
      binaryLaws[OneElem],
      binaryLaws[TwoElem],
      binaryLaws[Complex],
      binaryLaws[Complex](
        Binary[Complex].withChecksum(new java.util.zip.CRC32),
        implicitly,
        implicitly
      )
    )
  }

  val tuple2Auto = {
    implicit val instance = Binary.auto.derive[(Int, String)]
    binaryLaws[(Int, String)]
  }

  sealed trait Cases[A, B]
  case class Case1[A, B](a: A) extends Cases[A, B]
  case class Case2[A, B](b: B) extends Cases[A, B]

  sealed trait Tree[A]
  case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A]
  case class Leaf[A](item: A) extends Tree[A]

  val `multi-case class instances` = {
    import Binary.auto._

    Properties.list(
      binaryLaws[Cases[OneElem, TwoElem]],
      binaryLaws[Cases[Complex, Complex]],
      binaryLaws[Tree[Int]],
      binaryLaws[Tree[Complex]]
    )
  }

  val `checksum should complain when broken` = forAll { n: Long =>
    val binary = Binary[Long].withChecksum(new java.util.zip.CRC32)
    val encoded = binary encode n
    // let's manipulate the last byte of the checksum
    val manipulated = encoded.init :+ (encoded.last + 1).toByte
    val result = binary decode manipulated
    result.isEmpty
  }

}
