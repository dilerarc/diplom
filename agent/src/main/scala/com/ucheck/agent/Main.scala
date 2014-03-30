package com.ucheck.agent

import akka.actor.{Props, ActorSystem}
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory

class Main extends Bootable {

  val agentSystem = ActorSystem("agentSystem", ConfigFactory.load("agent"))

  def startup() = {
    println("Agent system started.")
  }

  def shutdown() = {
    agentSystem.shutdown()
  }
}
