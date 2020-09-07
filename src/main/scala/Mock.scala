import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.reflect.runtime.universe._


class MockContext {


}

class Mock[T: WeakTypeTag] {


}
//
//class OngoingStubbing[T](mock: Mock[T])
//   {
//    def getMock = mock
//}

object MockMacros {
  def when[T](mock: Mock[T]) : T = macro whenImpl[T]

  def whenImpl[T: c.WeakTypeTag](c: blackbox.Context)(mock: c.Expr[Mock[T]]): c.Expr[T] = {
    import c.universe._

    val mockingType = weakTypeOf[T]
    val className = weakTypeOf[T].getClass().getName()
//    val result = Block(
//        List(
//            ClassDef(
//                Modifiers(FINAL),
//                TypeName("$anon"),
//                List(),
//                Template(
//                    List(TypeTree(mockingType)),
//                    noSelfType
//                ))),
//                callConstructor(New(Ident(anon)))
//    )
    val result = q"""new ${mockingType.resultType}() {
        override def fooify(s: Int) = s + 6
    }"""
    c.Expr(result)
  }

  def mock[T](obj: T) : Mock[T] = macro mockImpl[T]
  def mockImpl[T](c: blackbox.Context)(obj: c.Expr[T]): c.Expr[Mock[T]] = {
    import c.universe._
      c.Expr(q"""
      
      """)

  }

}