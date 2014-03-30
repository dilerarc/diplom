package com.ucheck.agent

import scala.concurrent.duration._
import akka.actor._
import scala.sys.process._
import akka.actor.Identify
import com.ucheck.common.{JobResult, JobStop, Job}

class Agent extends Actor {

  val system = context.system
  var tasks = Map()[String, Cancellable]
  var base: ActorRef = null

  override def preStart(): Unit = {

    println("preStart")

    val selection = context.actorSelection("akka.tcp://baseSystem@127.0.0.1:2552/user/base")
    selection ! Identify

    val cmd = "atop 0 1" #| "grep java"
    val output = cmd.!!
    println(output)
  }

  override def receive: Actor.Receive = {

    case ActorIdentity(message, actor) if !actor.isEmpty ⇒
      base = actor.get
      base ! self

    case Job(itemId, command, updateInterval) ⇒
      tasks += itemId -> context.system.scheduler.schedule(0 seconds, updateInterval seconds) {
        base ! JobResult(itemId, format(command).!!)
      }

    case JobStop(itemId) ⇒
      tasks(itemId).cancel()
      tasks -= itemId
  }

  def format(command: String): ProcessBuilder = {
    command
  }
}