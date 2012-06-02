/// actors02.scala
/// Computes the factorial of a given number specified amount of times using several actors.
/// Round-robin load balancing is used to distribute work among actors.
/// Igor Shishkin, 2012.
///

import scala.actors._
import scala.actors.Actor._
import scala.annotation.tailrec
import scala.math.BigInt

object Factorial {
  def calculate(value: Int): BigInt = {

    @tailrec
    def factorialAcc(result: BigInt, next: Int): BigInt = {
      if (next > 1) factorialAcc(result * next, next - 1)
      else 1
    }    
    
    factorialAcc(1, value)
  }
}

case class Die()
case class FactorialTask(value: Int, cycles: Int, actors: Int)

class FactorialActor extends Actor {
  
  def act() {
  var live = true
    loopWhile(live) {
      react {
        case Die => live = false
        case value: Int => reply(Factorial.calculate(value))
      }
    }
  }
}

class ActorsTest extends Actor {
  def act() {
    react {
      case FactorialTask(value, cycles, actors) =>
        val start = System.currentTimeMillis()
        val target = sender
        val processors = new Array[Actor](actors)
        for (i <- 0 until actors) {
          processors(i) = new FactorialActor()
          processors(i).start()
        }

        // sending tasks
        var j = 0
        for (i <- 0 until cycles) {
          processors(j) ! value
          j += 1
          if (j >= processors.length) j = 0
        }
        
        // stop workers
        for (i <- 0 until actors) processors(i) ! Die
        
        // waiting for replies
        var replies = 0
        var result = 0
        mkBody {
          loopWhile (replies < cycles) {
            react {
              case value: BigInt =>
                result += value.byteValue()
                replies += 1
            }
          }
        } andThen {
          val end = System.currentTimeMillis()
          target ! (end - start, result)
        }
    }
  }
}

object Main {
  def main(args: Array[String]) {
    if (args.length < 3) {
      Console.println("Parameters: value cycles actors_number")
      return
    }

    val value = args(0).toInt
    val cycles = args(1).toInt
    val actors = args(2).toInt

    Console.println("Hello!")
    testSequential(value, cycles)
    testActors(value, cycles, actors)
    Console.println("Bye!");
  }
  
  private def testSequential(value: Int, cycles: Int) {
    var result = 0    
    val start = System.currentTimeMillis
    for (i <- 0 until cycles) result += Factorial.calculate(value).byteValue()
    val end = System.currentTimeMillis
    Console.println("Serial: " + (end - start) + "ms, result is " + result.toString)
  }
  
  private def testActors(value: Int, cycles: Int, actors: Int) {
    val a = new ActorsTest()
    a.start()
    val (time, result) = a !? FactorialTask(value, cycles, actors)
    Console.println("Actors: " + time + "ms, result is " + result.toString)
  }
}
