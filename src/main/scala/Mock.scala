import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.reflect.runtime.universe._
import scala.collection.mutable.Buffer

/* This is a library that supports creating "mock" objects
 * that have an interface that complies with a particular
 * class, but has dummy values, supplied by a user, for
 * the purpose of testing.
 * 
 * Usage:
 * 
 * // fooMock is an object with the same interface as Foo
 * val fooMock = mock[Foo]
 * // when the fooify method is called with 3, it will now
 * return 10
 * when(fooMock.fooify(3)).thenReturn(10)
 *
*/
class MockUndefinedException(s:String) extends Exception(s)

class MockContext {
  val handlers : Buffer[Any] = Buffer[Any]()

  var currentMockMethod: (Mock[_], String, Any) = null 

  def appendHandler[Value](value: Value) = {
    val fullCall = currentMockMethod match {
      case (mock, methodName, arg) => (mock, methodName, arg, value)
    }
     handlers.append(fullCall)
  }

  def setCurrentMockMethod[Arg](mock: Mock[_], funcName: String, arg: Arg) = {
    currentMockMethod = (mock, funcName, arg)
  }

  /* Given a mock object, function name, and argument, return
   * a matching return value if there is one.
   */
  def findMatchingHandler(mock: Mock[_], funcName: String, arg: Any): Option[Any] = {
    handlers.collect { handler =>
      handler match {
        case (savedMock, savedFunctionName, savedArg, value) if mock == savedMock && funcName == savedFunctionName && arg == savedArg => 
          value
      }
    }.headOption
  }
}

/* This trait exists for better type-checking in the MockContext class */
trait Mock[T]

trait Mocking {
  implicit val mockContext = new MockContext
}

class Stubbing[T](implicit val mockContext: MockContext) {
  def thenReturn(returnVal: T) = {
    mockContext.appendHandler(returnVal)
  }
}

object MockHelpers {
  import scala.language.experimental.macros

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
    /* By default, `members` returns fields and methods
     * that are common to all objects. Since we do not
     * want to override all of these, we filter out the ones
     * that belong to Object.
     * 
     * This logic I borrowed from:https://github.com/paulbutcher/ScalaMock/blob/master/shared/src/main/scala/org/scalamock/clazz/MockMaker.scala#L308
     * 
     */
    val methodDefs = mockingType.members.filter { member => 
      member.isMethod && !member.isConstructor && !isMemberOfObject(member) && !member.isPrivate && !member.isFinal
    }.map { member => 
      val method = member.asMethod
      val returnType = method.returnType
      /* It's required that param lists are a sequence of 
       * ValDefs
       */
      val paramsString = method.paramLists.map { paramList => 
        paramList.map {  symbol =>
          q"""val ${symbol.name.toTermName}: ${symbol.typeSignature}"""
        }
      }

      val name = method.name
      /* This currently only works with methods that have a single
       * parameter */
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