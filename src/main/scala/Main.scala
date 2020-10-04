import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.reflect.runtime.universe._
import shapeless._
import shapeless.ops.hlist
import MockHelpers._

class Foo {
  def fooify(s: Int) = s + 3
  def barify(t: String) = t + "bar"
}

object Main extends App with Mocking {
  val myMock = mock[Foo]
  when(myMock.fooify(7)).thenReturn(200)
  when(myMock.barify("some string")).thenReturn("yet another string")
  println(myMock.fooify(7))
  println(myMock.barify("some string"))
}
