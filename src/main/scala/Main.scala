import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.reflect.runtime.universe._
import shapeless._
import shapeless.ops.hlist

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



object Main extends App with MockMatchers {
  println("Hello")
  val mock = MockMacros.mock[Foo3]
  println(mock.fooify(4))
  println(mock)
//
//println(fooifyResult)
//val mock = MockMacros.mock[Foo3]
//        val func = (mock.fooify _)
//        func.k
//        when(4)
//
 //val mock = MockMacros.mock[Foo3]
//val mock = MockMacros.mock[Foo3]
 //println(mock.mockStuff)
// val funcObject = (mock.fooify _).when(3)
// println(mock.fooify(2))
// //println(mock.fooify(_).when(9))
// val stubObject = MockMacros.when(mock)
 //println(stubObject.fooify(10))
}
