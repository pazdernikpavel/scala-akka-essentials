package testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class TimedAssertionsSpec extends
  TestKit(ActorSystem("TimedAssertionsSpec", ConfigFactory.load().getConfig("specialTimedAssertionsConfig")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TimedAssertionsSpec._

  "A worker" should {
    val worker = system.actorOf(Props[Worker])

    "reply with meaning of life in a timely manner" in {
      within(500 millis, 1 second) {
        worker ! "work"
        expectMsg(WorkResult(42))
      }
    }

    "reply with valid work at a reasonable cadence" in {
      within(1 second) {
        worker ! "workSequence"

        val results: Seq[Int] = receiveWhile[Int](max = 2 seconds, idle = 500 millis, messages = 10) {
          case WorkResult(result) => result
        }

        assert(results.sum > 5)

      }
    }

    "reply to a test probe in a timely manner" in {
      within(1 second) {
        val probe = TestProbe()
        probe.send(worker, "work")
        probe.expectMsg(WorkResult(42))
        // Fails, test probes have their own config, in this example 0.3s timeout from conf
      }
    }

  }

}

object TimedAssertionsSpec {
  case class WorkResult(result: Int)

  class Worker extends Actor {
    override def receive: Receive = {
      case "work" =>
        Thread.sleep(500)
        sender ! WorkResult(42)
      case "workSequence" =>
        val random = new Random()
        for (i <- 1 to 10) {
          Thread.sleep(random.nextInt(50))
          sender ! WorkResult(1)
        }
    }
  }

}
