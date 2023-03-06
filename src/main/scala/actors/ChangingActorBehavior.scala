package actors

import actors.ChangingActorBehavior.Mom.MomStart
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehavior extends App {

  object FussyKid {

    case object KidAccept

    case object KidReject

  }

  class StatelessFussyKid extends Actor {

    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, discardOld = false) // change my receive behavior to sad receive
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, discardOld = false) // stay sad
      case Food(CHOCOLATE) => context.unbecome() // change my receive behavior to happy receive
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
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("Do you want to play?")
      case KidAccept => println("Yay, my kid is happy.")
      case KidReject => println("My kid is sad, but at least he is healthy...")
    }

  }

  val system = ActorSystem("changingActorBehavior")
  val mom = system.actorOf(Props[Mom], "mom")
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid], "statelessFussyKid")

  mom ! MomStart(statelessFussyKid)
  /*
  *   Mom receives MomStart
  *     Kid receives Food(vegetable) -> Kid will change the handler to SadReceive
  *     Kid receives Ask(play?) -> Kid replies with SadReceive handler
  *   Mom receives KidReject
  */

  /*
  *   Context.become(..., false) creates stack and history of previous contexts
  *   Stack:
  *     1. happyReceive
  *     2. sadReceive
  *     3. happyReceive
  */

  /*
  *   new behavior
  *   Food(Veg)
  *   Food(Veg)
  *   Food(Veg)
  *
  *   Stack:
  *     1. sadReceive
  *     2. sadReceive
  *     3. sadReceive
  *     4. happyReceive
  *
  *   Food(chocolate)
  *
  *   Stack:
  *     1. sadReceive
  *     2. sadReceive
  *     3. happyReceive
  */

  /* EXERCISE 1 */

  object Counter {
    case object Increment

    case object Decrement

    case object Print
  }

  class Counter extends Actor {

    import Counter._

    override def receive: Receive = counterReceiveHandler(0)

    def counterReceiveHandler(count: Int): Receive = {
      case Increment => context.become(counterReceiveHandler(count + 1), discardOld = false)
      case Decrement => context.become(counterReceiveHandler(count - 1), discardOld = false)
      case Print => println(s"Current counter is: $count")
    }

  }

  val counter = system.actorOf(Props[Counter], "counter")

  import Counter._

  counter ! Increment
  counter ! Increment
  counter ! Increment
  counter ! Print
  counter ! Increment
  counter ! Print
  counter ! Decrement
  counter ! Print

  /* EXERCISE 2 */

  case class Vote(candidate: String)

  case object VoteStatusRequest

  case class VoteStatusReply(candidate: Option[String])

  case class AggregateVotes(citizens: Set[ActorRef])

  case object PrintAggregatedVotes

  class Citizen extends Actor {
    override def receive: Receive = citizenReceiveHandler()

    def citizenReceiveHandler(votedCandidate: String = null): Receive = {
      case Vote(newCandidate) => context.become(citizenReceiveHandler(newCandidate))
      case VoteStatusRequest =>
        sender() ! VoteStatusReply(Option(votedCandidate))
    }
  }

  class VoteAggregator extends Actor {

    var votes: Map[String, Int] = Map()

    override def receive: Receive = {
      case VoteStatusReply(Some(candidate)) => {
        val currVotes = votes.getOrElse(candidate, 0)
        votes += (candidate -> (currVotes + 1))
      }
      case AggregateVotes(candidates) => candidates.foreach(candidate => candidate ! VoteStatusRequest)
      case PrintAggregatedVotes => println(votes)
    }

  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))
  voteAggregator ! PrintAggregatedVotes

  /*
    Print the status of the votes

    Martin -> 1
    Jonas -> 1
    Roland -> 2

  */

}
