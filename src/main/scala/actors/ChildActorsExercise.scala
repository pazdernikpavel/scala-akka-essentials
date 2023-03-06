package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercise extends App {

  /*
    Distributed word counting
  */

  object WordCounterMaster {
    case class Initialize(nChildren: Int)

    case class WordCountTask(id: Int, text: String)

    case class WordCountReply(id: Int, count: Int)
  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(children) => {
        println("[master] initializing...")
        val childrenRefs = for (i <- 1 to children) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
      }
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] i have received $text - I Will send it to child at current child index $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        context.become(withChildren(
          childrenRefs, (currentChildIndex + 1) % childrenRefs.length,
          currentTaskId + 1,
          requestMap + (currentTaskId -> originalSender)))

      case WordCountReply(id, count) =>
        println(s"[master] i have received a reply for task with ID $id with count $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"[worker ${self.path}] I have received task with $text")
        sender ! WordCountReply(id, text.split(" ").length)
    }
  }

  /*
    Create word counter master with init 10
    After init send some text and get count
  */

  class Requester extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I Love Akka", "Scala is super dope", "yes", "me too")
        texts.foreach(text => master ! text)
      case count: Int => println(s"[requester] I received a reply $count")
    }
  }

  val system = ActorSystem("roundRobinWordCountExercise")
  val requester = system.actorOf(Props[Requester], "requester")
  requester ! "go"
}
