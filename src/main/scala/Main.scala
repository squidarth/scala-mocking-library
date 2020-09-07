import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.reflect.runtime.universe._

//  object Macros {
//    def hello(s: String): Unit = macro helloImpl
//    def helloImpl(c: blackbox.Context)(s: c.Expr[String]): c.Expr[Unit] = {
//      import c.universe._
//      c.Expr(q"""println("hello" + ${s.tree})""")
//    }
//  }

class Foo3 {
  def fooify(s: Int) = s + 3
}



object Main extends App {
 val mock = MockMacros.mock[Foo3]
 println(mock.mockStuff)
 val stubObject = MockMacros.when(mock)
 println(stubObject.fooify(3))
}
