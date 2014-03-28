package models.sheduler

import akka.actor.{ActorIdentity, Identify, Actor}

class UcheckApp extends Actor {

  override def preStart(): Unit = {
    println("preStart")
    val actor = context.actorSelection("akka.tcp://agentSystem@192.168.74.128:2552/user/agent")
    println(actor)
    actor ! Identify("ident hello")
  }

  def receive = {
    case ActorIdentity(message, actor) => {
      actor.get ! "start"
    }
    case "gg" => {
      println("stop sent")
      sender ! "stop"
      context.stop(self)
    }
    case l: Long => {
      println(l)
    }
  }
}