/// actors01.scala
/// Example from "Actors in Scala" by Philipp Haller & Frank Sommers.
/// Recursively builds a chain of actors and then
/// sinks "Die" message and waits for a reply.
/// Igor Shishkin, 2012.
///

import scala.actors._
import scala.actors.Actor._

object Main {
    def buildChain(size: Int, next: Actor): Actor = {

        val a = actor {
            react {
                case "Die" =>
                    val from = sender
                    if (next != null) {
                        next ! "Die"
                        react {
                            case "Ack" => from ! "Ack"
                        }
                    }
                    else from ! "Ack"
            }
        }

        if (size > 0) buildChain(size - 1, a)
        else a
    }
    
    def main(args: Array[String]): Unit = {
        if (args.length < 1) {
            Console.println("Parameters: chain_length")
            return
        }

        val numActors = args(0).toInt
        Console.println("Building chain from " + numActors + " actors...")

        val start = System.currentTimeMillis
        buildChain(numActors, null) ! "Die"
        receive {
            case "Ack" =>
                val end = System.currentTimeMillis
                println("Took " + (end - start) + "ms")
        }

        Console.println("Press a key to continue...")
        System.in.read()
    }
}
