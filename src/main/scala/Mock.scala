import scala.language.experimental.macros
import scala.Nothing
import scala.reflect.macros.whitebox
import scala.reflect.runtime.universe._
import scala.collection.mutable.{Seq => MutableSeq} 

class MockContext {
  var handlers : Seq[Any] = Seq[Any]()

  var currentMockMethod: (String, Any) = null 

  def appendHandler[T](value: T) = {
    val fullCall = currentMockMethod match {
      case (methodName, arg) => (methodName, arg, value)
    }
    handlers = handlers ++ Seq(fullCall)
  }
}

trait Mock[T] {
}




trait MockMatchers {
  import scala.language.implicitConversions

  implicit val mockContext = new MockContext

//  protected def * = new MatchAny
//  protected implicit def toMockParameter[T](v: T) = new MockParameter(v)
//  protected implicit def matcherBaseToMockParameter[T](m: MatcherBase) = new MockParameter[T](m)
//  protected implicit def stubFunction[A, V](f: Function1[A,V]) = f.asInstanceOf[FakeFunction[A,V]]

}

class ArgumentMatcher(template: Product) extends Function1[Product, Boolean] {
  
  def apply(args: Product) = template == args
  
  override def toString = template.productIterator.mkString("(", ", ", ")")
}

class FunctionAdapter1[T1, R](f: T1 => R) extends Function1[Product, R] {
  
  def apply(args: Product) = {
    assert(args.productArity == 1)
    f(args.productElement(0).asInstanceOf[T1])
  }
}

class FakeFunction[A, V] extends Function1[A, V] {
    val returnVal: Option[V] = None

//    def when(v1: MockParameter[A]) = println("when called 1")
//    def when(matcher: FunctionAdapter1[A, Boolean]) = println("when called")
   
    def apply(v1: A) : V = returnVal match {
        case Some(v) => v.asInstanceOf[V]
        case None => throw new Exception("Undefined mock value")
    }
}

class Stubbing[T](implicit val mockContext: MockContext) {
  def thenReturn(returnVal: T) = {
    mockContext.handlers

  }
}

object MockMacros {
  import scala.language.experimental.macros
  import scala.language.implicitConversions
//  def when[T](mock: Mock[T]) : T = macro whenImpl[T]
//
//  def whenImpl[T: c.WeakTypeTag](c: blackbox.Context)(mock: c.Expr[Mock[T]]): c.Expr[T] = {
//    import c.universe._
//
//    val mockingType = weakTypeOf[T]
//
//    val result = q"""new ${mockingType.resultType}() {
//        override def fooify(s: Int) = s + 6 + ${mock}.mockStuff
//    }"""
//    c.Expr(result)
//  }

  def when[T](returnVal: T)(implicit mockContext: MockContext): Stubbing[T] = {
    new Stubbing[T]()
  }



  def mock[T](implicit mockContext: MockContext) : T with Mock[T] = macro mockImpl[T]
  def mockImpl[T: c.WeakTypeTag](c: whitebox.Context)(mockContext: c.Expr[MockContext]): c.Expr[T] = {
    import c.universe._

    def isMemberOfObject(s: c.universe.Symbol) = {
      val res = TypeTag.Object.tpe.member(s.name)
      res != NoSymbol && res.typeSignature == s.typeSignature
    }

    val mockingType = weakTypeOf[T]
    val methodDefs = mockingType.members.filter { member => 
      member.isMethod && !member.isConstructor && !isMemberOfObject(member) && !member.isPrivate && !member.isFinal
    }.map { member => 
      val method = member.asMethod
      val returnType = method.returnType
      val paramsString = method.paramLists.map { paramList => 
        paramList.map {  symbol =>
          q"""val ${symbol.name.toTermName}: ${symbol.typeSignature}"""
        }
      }
      
      val name = method.name

      q"""
      override def ${name.toTermName}(...${paramsString}) : ${returnType} = {
         throw new Exception("hello") 
      }
      """
    }.toList

    var classBody = q"""
     ..${methodDefs}
    """
    val result = q"""new ${mockingType.resultType} with Mock[${mockingType.resultType}] { ..$classBody}"""
    c.Expr(result)
  }
}