package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello there!"
      case message: String => println(s"[simple actor] I Have received a message: $message")
      case number: Int => println(s"[simple actor] I have received a number: $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received special message: $contents")
      case SendMessageToYourself(message) => context.self ! message
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward content // I keep original sender
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  // 1 - Messages can be any type
  simpleActor ! "hey" // [simple actor] I Have received a message: hey
  simpleActor ! 42 // [simple actor] I have received a number: 42

  case class SpecialMessage(contents: String)

  simpleActor ! SpecialMessage("Some special content!")

  // 2 - Actors have access to self property
  case class SendMessageToYourself(message: String)

  simpleActor ! SendMessageToYourself("Sending message to myself!")

  // 3 - Actors can reply to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4 - Dead letters
  alice ! "Hi!" // no implicit sender
  // Message is sent to built-in garbage pool called dead letters

  // 5 - Forwarding messages
  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi", bob)


}
