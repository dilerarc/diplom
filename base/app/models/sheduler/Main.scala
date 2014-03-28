package models.sheduler

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable

class Main extends Bootable {

  val baseSystem = ActorSystem("baseSystem", ConfigFactory.load("base"))

  def startup = {
    baseSystem.actorOf(Props[UcheckApp], "base")
    println("Base system started.")
  }

  def shutdown = {
    baseSystem.shutdown()
  }
}
