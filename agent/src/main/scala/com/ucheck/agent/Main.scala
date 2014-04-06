package com.ucheck.agent

import akka.actor.{Props, ActorSystem}
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import java.io.File

class Main extends Bootable {
  private val file: File = new java.io.File("config/agent.conf")
  println(file.getAbsolutePath)
  val config = ConfigFactory.parseFile(file).withFallback(ConfigFactory.load())

  val agentSystem = ActorSystem("agentSystem", config)

  def startup() = {
    println("Agent system started.")
    agentSystem.actorOf(Props[Agent], "agentActor")
  }

  def shutdown() = {
    agentSystem.shutdown()
  }
}
