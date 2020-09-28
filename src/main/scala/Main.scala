import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.reflect.runtime.universe._
import shapeless._
import shapeless.ops.hlist

class Foo3 {
  def fooify(s: Int) = s + 3
}

object Main extends App with MockMatchers {
  val fooObject = new Foo3


val mock = MockMacros.mock[Foo3]
//println(mock.fooify(10))
MockMacros.when(mock.fooify(7)).thenReturn(200)
println(mock.fooify(7))

}
