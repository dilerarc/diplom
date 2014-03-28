package com.ucheck.agent

import scala.concurrent.duration._
import akka.actor.Actor
import scala.sys.process._

class Agent extends Actor {

  val system = context.system

  override def preStart(): Unit = {
    val cmd = "atop 0 1" #| "grep java"
    val output = cmd.!!
    println(output)
    println("preStart")
  }

  override def receive: Actor.Receive = {
    case "start" => {
      println("start")
      val base = sender
      import system.dispatcher
      system.scheduler.schedule(150 milliseconds, 150 milliseconds) {
        base ! System.currentTimeMillis
      }
      context.system.scheduler.scheduleOnce(5 seconds, base, "gg")

    }
    case "stop" => {
      println("stop")
      context.stop(self)
    }
  }
}