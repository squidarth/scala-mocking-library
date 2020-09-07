import org.scalatest._
import flatspec._
import matchers._

class Foo2(val string: String) {
  def fooify(s: String) : String = {
    string + s + "FOO"
  }
}

class NewSpec extends AnyFlatSpec 
  with should.Matchers 
  {


      "Fooify" should "work correctly" in {
          """
  val mock = new Mock[Foo3]
  val stubbing = MockMacros.when(mock)
  println(mock)
  println(stubbing.fooify(3))
          """ should compile
      }

  }
