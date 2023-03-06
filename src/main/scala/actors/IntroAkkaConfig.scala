package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {
  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  // 1. inline config
  val configString =
    """
      | akka {
      |   logLevel = "DEBUG"
      | }
      |""".stripMargin

  val config = ConfigFactory.parseString(configString);
  val system = ActorSystem("ConfigDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])
  actor ! "message to remember"


  // 2. application config
  val defaultConfigFileSystem = ActorSystem("DefaultConfig")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  defaultConfigActor ! "remember me"

  // 3. special config
  val specialConfig = ConfigFactory.load().getConfig("specialConfig")
  val specialConfigFileSystem = ActorSystem("SpecialConfigSystem", specialConfig)
  val specialConfigActor = specialConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "i feel really special"

}
