package actors

import actors.ChangingActorBehavior.Mom.{CHOCOLATE, MomStart}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehavior extends App {

  object FussyKid {

    case object KidAccept

    case object KidReject

    val HAPPY = "happy"
    val SAD = "sad"

  }

  class FussyKid extends Actor {

    import FussyKid._
    import Mom._

    var state = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }

  }

  class StatelessFussyKid extends Actor {

    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive) // change my receive behavior to sad receive
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => // stay sad
      case Food(CHOCOLATE) => context.become(happyReceive) // change my receive behavior to happy receive
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {

    case class MomStart(kidRef: ActorRef)

    case class Food(food: String)

    case class Ask(message: String)

    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"

  }

  class Mom extends Actor {

    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) => {
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("Do you want to play?")
      }
      case KidAccept => println("Yay, my kid is happy.")
      case KidReject => println("My kid is sad, but at least he is healthy...")
    }

  }

  val system = ActorSystem("changingActorBehavior")
  val kid = system.actorOf(Props[FussyKid], "fussyKid")
  val mom = system.actorOf(Props[Mom], "mom")
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid], "statelessFussyKid")

  mom ! MomStart(kid)
  mom ! MomStart(statelessFussyKid)

}