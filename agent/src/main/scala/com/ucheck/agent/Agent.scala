package com.ucheck.agent

import scala.concurrent.duration._
import akka.actor._
import scala.sys.process._
import com.ucheck.common.{JobStop, JobResult, Job}

class Agent(base: ActorRef, job: Job) extends Actor {

  import context.dispatcher

  var task:Cancellable = null

  override def preStart(): Unit = {

    println("preStart")

    task = context.system.scheduler.schedule(5 seconds, job.updateInterval seconds) {
      println(this)
      base ! JobResult(job.itemId, format(job.command).!!)
    }

/*    val cmd = "atop 0 1" #| "grep java"
    val output = cmd.!!
    println(output)*/
  }

  override def receive: Actor.Receive = {

    case JobStop => {
      task.cancel()
      self ! PoisonPill
    }
  }

  private def format(command: String): ProcessBuilder = {
    command
  }
}

object Agent {
  def apply(base:ActorRef, job:Job): Props = Props(classOf[Agent], base, job)
}