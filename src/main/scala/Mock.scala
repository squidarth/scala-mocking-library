import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.reflect.runtime.universe._


class MockContext {


}

trait Mock[T] {
    def mockStuff = 100
}

object MockMacros {
  def when[T](mock: Mock[T]) : T = macro whenImpl[T]

  def whenImpl[T: c.WeakTypeTag](c: blackbox.Context)(mock: c.Expr[Mock[T]]): c.Expr[T] = {
    import c.universe._

    val mockingType = weakTypeOf[T]

    val result = q"""new ${mockingType.resultType}() {
        override def fooify(s: Int) = s + 6 + ${mock}.mockStuff
    }"""
    c.Expr(result)
  }

  def mock[T] : T with Mock[T] = macro mockImpl[T]
  def mockImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[T] = {
    import c.universe._

    val mockingType = weakTypeOf[T]
    val result = q"""new ${mockingType.resultType} with Mock[${mockingType.resultType}] {
        override def fooify(s: Int) = s + 7
    }"""
    c.Expr(result)
  }
}

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