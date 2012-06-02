/// functional01.scala
/// Simple closure and currying example.
/// Igor Shishkin, 2012.
///

object Main {
  
	def curry_concat(a: String)(b: String) = a + b

	def makeFactor(factor: Int) = (x: Int) => factor * x
  
	def main(args: Array[String]) : Unit = {
	  // closure: binds visible parameters
	  val f1 = makeFactor(10)
	  println(f1(1))
	  println(f1(5))
	  
	  // curring: binary function -> function of function
	  println(curry_concat("Hello")(" world!"))
	  val f2 = curry_concat("Hello, ")_
	  println(f2("James"))
	  println(f2("Molly"))
	}
}
