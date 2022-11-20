package recap

object GeneralRecap extends App {
  val aCondition: Boolean = false;

  var aVariable = 42
  aVariable += 1 // aVariable = 43

  val aConditionedVal = if (aCondition) 42 else 65

  val aCodeBlock = {
    if (aCondition) 74
    56
  }

  val theUnit = println("Hello, Scala!")

  def aFunction(x: Int) = x + 1

  def factorial(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else factorial(n - 1, acc * n)

  class Animal

  class Dog extends Animal

  val aDog: Animal = new Dog

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  class Crododile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("Crunchy crunch..")
  }

  val aCrododile = new Crododile
  aCrododile.eat(aDog)
  aCrododile eat aDog

  val anonymousClass = new Carnivore {
    override def eat(a: Animal): Unit = println("nom nom nom!")
  }

  anonymousClass eat aDog

  abstract class MyList[+A]

  object MyList

  case class Person(name: String, age: Int)

  val potentialFailure = try {
    throw new RuntimeException("I am innocent")
  } catch {
    case e: Exception => "I caught an exception!"
  } finally {
    println("some logs..")
  }

  val incrementer = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(43) // 44

  val anonymousIncrementer = (x: Int) => x + 1

  List(1, 2, 3).map(anonymousIncrementer)

  val anOption = Some(2)

  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n"
    case _ => "I dont know my name"
  }
}
