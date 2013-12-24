package shapeless.contrib

package object scalaz extends Instances with Functions with Lifts with Lenses with FreeInstances{

  object instances extends Instances

  object functions extends Functions

  object lift extends Lifts

  object lenses extends Lenses

  object binary extends BinarySyntax

  object free extends FreeInstances

}

// vim: expandtab:ts=2:sw=2
