package testing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class InterceptingLogSpec extends TestKit(ActorSystem("InterceptingLogSpec", ConfigFactory.load().getConfig("interceptingLogMessages")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import InterceptingLogSpec._

  val item = "rock the jvm"
  val creditCard = "1234-1234-1234-1234"

  "checkout flow" should {
    "correctly log the dispatch of an order" in {
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched.") intercept {
        val checkoutActor = system.actorOf(Props[CheckoutActor])
        checkoutActor ! Checkout(item, creditCard)
      }
    }
  }
}

object InterceptingLogSpec {

  case class Checkout(item: String, creditCard: String)

  case class DispatchOrder(item: String)

  case class AuthorizeCard(creditCard: String)

  case object PaymentAccepted

  case object PaymentDenied

  case object OrderConfirmed

  class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentManager])

    override def receive: Receive = awaitingCheckout

    def awaitingCheckout: Receive = {
      case Checkout(item, card) =>
        paymentManager ! AuthorizeCard(card)
        context.become(pendingPayment(item))
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
      case PaymentDenied => context.become(awaitingCheckout)
    }

    def pendingFulfillment(item: String): Receive = {
      case OrderConfirmed => context.become(awaitingCheckout)
    }
  }

  class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(card) =>
        if (card.startsWith("0")) sender() ! PaymentDenied
        else sender() ! PaymentAccepted
    }
  }

  class FulfillmentManager extends Actor with ActorLogging {
    var orderId = 0

    override def receive: Receive = {
      case DispatchOrder(item: String) =>
        orderId += 1
        log.info(s"Order $orderId to item $item has been dispatched.")
        sender() ! OrderConfirmed
    }
  }

}
