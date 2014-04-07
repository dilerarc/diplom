package com.ucheck.agent

import akka.actor.{Props, ActorSystem}
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import java.io.File

class App extends Bootable {

  val config = ConfigFactory.parseFile( new java.io.File("config/agent.conf")).withFallback(ConfigFactory.load())

  val agentSystem = ActorSystem("agentSystem", config)

  def startup() = {
    println("Agent system started.")
    agentSystem.actorOf(Props[Manager], "manager")
  }

  def shutdown() = {
    agentSystem.shutdown()
  }
}
