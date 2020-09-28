import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.reflect.runtime.universe._
import shapeless._

class MockUndefinedException(s:String) extends Exception(s)

class MockContext {
  var handlers : Seq[Any] = Seq[Any]()

  var currentMockMethod: (Mock[_], String, Any) = null 

  def appendHandler[Value](value: Value) = {
    val fullCall = currentMockMethod match {
      case (mock, methodName, arg) => (mock, methodName, arg, value)
    }
    handlers = handlers ++ Seq(fullCall)
  }

  def setCurrentMockMethod[Arg](mock: Mock[_], funcName: String, arg: Arg) = {
    currentMockMethod = (mock, funcName, arg)
  }

  def findMatchingHandler(mock: Mock[_], funcName: String, arg: Any): Option[Any] = {
    handlers.collect { handler =>
      handler match {
        case (savedMock, savedFunctionName, savedArg, value) if mock == savedMock && funcName == savedFunctionName && arg == savedArg => 
          value
      }
    }.headOption
  }
}

trait Mock[T]

trait Mocking {
  import scala.language.implicitConversions

  implicit val mockContext = new MockContext
}

class Stubbing[T](implicit val mockContext: MockContext) {
  def thenReturn(returnVal: T) = {
    mockContext.appendHandler(returnVal)
  }
}

object MockHelpers {
  import scala.language.experimental.macros
  import scala.language.implicitConversions

  def when[T](getReturnVal: => T)(implicit mockContext: MockContext): Stubbing[T] = {
    try {
      getReturnVal
    } catch {
      case e: MockUndefinedException => ()
      case e: Throwable => throw e
    }
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
      val firstParamName = method.paramLists.headOption.flatMap(_.headOption).map { symbol => symbol.name}.get

      q"""
      override def ${name.toTermName}(...${paramsString}) : ${returnType} = {
        ${mockContext}.setCurrentMockMethod(this, ${name.toString()}, ${firstParamName.toTermName})
        val foundHandler = ${mockContext}.findMatchingHandler(this, ${name.toString()}, ${firstParamName.toTermName})
        foundHandler match {
          case Some(value) => value.asInstanceOf[${returnType}]
          case None => throw new MockUndefinedException("no mock found")
        }
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