package actors

import actors.Exercise.BankAccount.{Deposit, Withdraw}
import actors.Exercise.Person.LiveTheLife
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Exercise extends App {

  val actorSystem = ActorSystem("exerciseActorSystem")

  object CounterActor {
    case object Increment

    case object Decrement

    case object Print
  }

  class CounterActor extends Actor {

    import CounterActor._

    var count = 0

    override def receive: Receive = {
      case Increment => {
        count += 1
        println("Incremented!")
      }
      case Decrement => {
        count -= 1
        println("Decremented!")
      }
      case Print => println(s"Current count is: $count")
    }
  }

  val counterActor = actorSystem.actorOf(Props[CounterActor], "counterActor")

  (1 to 5).foreach(_ => counterActor ! CounterActor.Increment)
  (1 to 3).foreach(_ => counterActor ! CounterActor.Decrement)
  counterActor ! CounterActor.Print

  object BankAccount {
    case class Deposit(amount: Int)

    case class Withdraw(amount: Int)

    case object Statement

    case class TransactionSuccess(message: String)

    case class TransactionFailure(message: String)
  }

  class BankAccount extends Actor {

    import BankAccount._

    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) => {
        if (amount < 0) sender() ! TransactionFailure("Invalid deposit amount!")
        else {
          funds += amount
          sender() ! TransactionSuccess(s"Successfully deposited $amount")
        }
      }
      case Withdraw(amount) => {
        if (amount < 0) sender() ! TransactionFailure("Invalid withdraw amount!")
        else if (amount > funds) sender() ! TransactionFailure("Insufficient balance!")
        else {
          funds -= amount
          sender() ! TransactionSuccess(s"Successfully withdrawn $amount")
        }
      }
      case Statement => println(s"There is currently $funds available on your bank account.")
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {

    import Person._
    import BankAccount._

    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(20000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)
    }
  }

  val account = actorSystem.actorOf(Props[BankAccount], "bankAccount")
  val person = actorSystem.actorOf(Props[Person], "billionaire")

  person ! LiveTheLife(account)

}
