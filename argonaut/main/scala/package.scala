package shapeless.contrib

import _root_.scalaz.{Coproduct => _, _}
import _root_.shapeless._
import _root_.argonaut.{DecodeJson, DecodeResult, EncodeJson, Json}

package object argonaut{

  implicit val DecodeJsonI: ProductTypeClass[DecodeJson] =
    new ProductTypeClass[DecodeJson] {
      val emptyProduct =
        DecodeJson[HNil](Function const DecodeResult.ok(HNil))

      def product[H, T <: HList](f: DecodeJson[H], t: DecodeJson[T]) =
        DecodeJson[H :: T](c =>
          Apply[DecodeResult].apply2(f(c), t(c))(_ :: _)
        )

      def project[F, G](instance: => DecodeJson[G], to: F => G, from: G => F) =
        instance.map(from)
    }

  implicit val EncodeJsonI: ProductTypeClass[EncodeJson] =
    new ProductTypeClass[EncodeJson] {
      val emptyProduct =
        EncodeJson[HNil](Function const Json.jNull)

      def product[H, T <: HList](f: EncodeJson[H], t: EncodeJson[T]) =
        EncodeJson[H :: T]{
          case a :: b => Json.array(f(a), t(b))
        }

      def project[F, G](instance: => EncodeJson[G], to: F => G, from: G => F) =
        instance.contramap(to)
    }

  implicit def deriveDecodeJson[T]: DecodeJson[T] =
    macro TypeClass.derive_impl[DecodeJson, T]

  implicit def deriveEncodeJson[T]: EncodeJson[T] =
    macro TypeClass.derive_impl[EncodeJson, T]
}

