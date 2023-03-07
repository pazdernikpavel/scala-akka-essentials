package testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class TestProbeSpec extends TestKit(ActorSystem("TestProbeSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A master actor" should {
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)

      expectMsg(RegistrationAcknowledged)
    }

    "send the work to the slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAcknowledged)

      val workLoad = "count my text, now!"
      master ! Work(workLoad)

      slave.expectMsg(SlaveWork(workLoad, testActor))
      slave.reply(WorkCompleted(3, testActor))

      expectMsg(MessageReport(3))
    }

    "aggregate date correctly" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAcknowledged)

      val workLoad = "count my text, now!"
      master ! Work(workLoad)
      master ! Work(workLoad)

      slave.receiveWhile() {
        case SlaveWork(`workLoad`, `testActor`) => slave.reply(WorkCompleted(3, testActor))
      }

      expectMsg(MessageReport(3))
      expectMsg(MessageReport(6))
    }
  }
}

object TestProbeSpec {

  case object RegistrationAcknowledged

  case class Register(slaveRef: ActorRef)

  case class Work(text: String)

  case class WorkCompleted(count: Int, originalRequester: ActorRef)

  case class MessageReport(newTotalWordCount: Int)

  case class SlaveWork(text: String, originalRequester: ActorRef)

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegistrationAcknowledged
        context.become(online(slaveRef, 0))
      case _ => // ignore
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotalWordCount = totalWordCount + count
        originalRequester ! MessageReport(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))
    }
  }
}