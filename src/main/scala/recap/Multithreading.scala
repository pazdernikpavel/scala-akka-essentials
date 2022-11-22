package recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Multithreading extends App {
  val aThread = new Thread(() => println("I am running!"))
  aThread.start()
  aThread.join()

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  // different runs produce different results

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money

    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }
  }

  // inter-thread communication in JVM
  // wait - notify

  // Scala futures

  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future {
    // long computation
    42
  }

  future.onComplete {
    case Success(42) => println("I've found meaning of life!")
    case Failure(_) => println("Something went wrong...")
  }

  val aProccessedFuture = future.map(_ + 1) // Future(43)
  val aFlatFuture = future.flatMap { value =>
    Future(value + 2)
  }

  val nonsenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- aFlatFuture
  } yield meaningOfLife + filteredMeaning


}
