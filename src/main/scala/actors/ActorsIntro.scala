package actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // 1. Actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // 2. Create actors
  class WordCountActor extends Actor {
    var totalWords = 0

    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        totalWords += message.split(" ").length
        println(s"[word counter] Message received: ${message}")
      case message => println(s"[word counter] I cannot understand ${message.toString}")
    }
  }

  // 3. Instantiate actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // 4. Async communication
  wordCounter ! "I am learning Akka!"
  anotherWordCounter ! "A different message!"
  // ! equals to .tell

}
