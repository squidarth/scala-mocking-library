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
  with MockMatchers
  {

    "Test mocking" should "work correctly" in {
        5 should be(5)
        """

        val mock = MockMacros.mock[Foo3]
        (mock.fooify _).when(4)

        """ should compile

    }

//      "Fooify" should "work correctly" in {
//          """
//  val mock = new Mock[Foo3]
//  val stubbing = MockMacros.when(mock)
//  println(mock)
//  println(stubbing.fooify(3))
//          """ should compile
//      }

  }
