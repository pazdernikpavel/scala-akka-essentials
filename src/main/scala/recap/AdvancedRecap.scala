package recap

import scala.concurrent.Future
import scala.language.implicitConversions

object AdvancedRecap extends App {
  // Partial functions
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  // Equals
  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val function: (Int => Int) = partialFunction

  val modifiedList = List(1, 2, 3).map({
    case 1 => 42
    case _ => 0
  })

  // lifting
  val lifted = partialFunction.lift
  lifted(2) // Some(65)
  lifted(5000) // None

  // orElse
  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }

  pfChain(5) // 999 per partialFunction
  pfChain(60) // 9000
  pfChain(457) // throw match error

  // type aliases
  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => println("hello")
    case _ => println("confused")
  }

  // implicits
  implicit val timeout: Int = 3000

  def setTimeout(f: () => Unit)(implicit timeout: Int): Unit = f()

  setTimeout(() => println("timeout")) // extra parameter omitted

  // implicit conversions
  // 1) implicit defs
  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  implicit def fromStringToPerson(string: String): Person = Person(string)

  "Peter".greet

  // fromStringToPerson("Peter").greet =>> automatically done by compiler

  // 2) implicit classes
  implicit class Dog(name: String) {
    def bark = println("bark!")
  }

  "Lassie".bark
  // new Dog("Lassie").bark

  // Implicit organization
  // local scope
  implicit val inversedOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1, 2, 3).sorted

  // imported scope

  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future {
    println("hello, future")
  }

  // companion objects
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  List(Person("Bob"), Person("Alice")).sorted
}
