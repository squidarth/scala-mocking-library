import org.scalatest._
import flatspec._
import matchers._
import org.scalamock.scalatest.MockFactory
/*
 * confusing things so far:
 *  1. Not using stub vs. mock leads to a very confusing error message,
 * that could be helped by having mock implement similar methods. Take
 * a look at the scalamock codebase and see what's possible here. maybe
 * you could spec out some better error messages here.
 *  2. Why is "value" in scope?
 */
class Foo(val string: String) {
  def fooify(s: String) : String = {
    string + s + "FOO"
  }
}

class SampleSpec extends AnyFlatSpec 
  with should.Matchers 
  with MockFactory {
  "Fooify" should "work correctly"  in {
    val foo = new Foo("so what")
    val mockedFoo =  mock[Foo]

    (mockedFoo.fooify _).expects("blah").returning("blah").once()
    mockedFoo.fooify("blah")
  }

  "Fooify" should "operate with when (correctly)" in {
    /* If mockedFoo.fooify is called with a different
     * value, it can be hard to see what's going on here.
     * Would be helpful if instead it printed out
     * what the calls were for that stub. See what's
     * possible with scalatest! */

     /* As a sidenote, I wonder if there's a way in ScalaTest
      * to have a way of building up some test context, so that
      * when the test fails, it automatically shows you what calls
      * were made to your stubs/mocks.
      */
    val mockedFoo = stub[Foo]
    (mockedFoo.fooify _).when("baz").returning("bar")
    val value = mockedFoo.fooify("baz")
    value should be ("bar")
  }

  "Fooify" should "work when mocking is used" in {
    val mockedFoo = mock[Foo]

    (mockedFoo.fooify _).expects("blah").returning("blah").once()
    val value = mockedFoo.fooify("baz")
    println(value.concat("garbage"))
    value should be ("blah")
  }

  "Fooify" should "operate with when" in {
    /* This throws a weird error, mock used instead
     * of stub, it says java ClassCastException." */
    val mockedFoo = mock[Foo]
    (mockedFoo.fooify _).when("foo").returns("bar")
    mockedFoo.fooify("baz")
    value should be ("bar")
  }
}