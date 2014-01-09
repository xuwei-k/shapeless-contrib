package shapeless.contrib

import _root_.scalaz.{Coproduct => _, _}
import _root_.shapeless._
import _root_.argonaut.{EncodeJson, Json, JsonObject}

package object argonaut{

  implicit val EncodeJsonI: ProductTypeClass[EncodeJson] =
    new ProductTypeClass[EncodeJson] {
      val emptyProduct =
        EncodeJson[HNil](Function const Json.jObject(JsonObject.empty))

      def product[H, T <: HList](h: EncodeJson[H], t: EncodeJson[T]) = ???

      override def namedProduct[H, T <: HList](h: EncodeJson[H], name: String, t: EncodeJson[T]) =
        EncodeJson[H :: T]{
          case a :: b => Json.jObject((name, h(a)) +: t(b).objectOrEmpty)
        }

      def project[F, G](instance: => EncodeJson[G], to: F => G, from: G => F) =
        instance.contramap(to)
    }

  implicit def deriveEncodeJson[T]: EncodeJson[T] =
    macro TypeClass.derive_impl[EncodeJson, T]
}

