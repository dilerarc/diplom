package com.ucheck.agent

import scala.concurrent.duration._
import akka.actor._
import com.ucheck.common.{JobsStop, JobResult, Job}
import scala.sys.process._
import scala.util.Random
import org.joda.time.DateTime

class Worker(sender: ActorRef, job: Job) extends Actor {

  import context.dispatcher

  var t2:Cancellable = null

  override def preStart(): Unit = {
    println ("start:" + job)

    t2 = context.system.scheduler.schedule(1 seconds, job.updateInterval seconds) {
      //sender ! JobResult(job.itemId, format(job.command).!!, new Date())
      sender ! JobResult(job.itemId, Random.nextLong().toString, DateTime.now)
    }



    /*    val cmd = "atop 0 1" #| "grep java"
        val output = cmd.!!
        println(output)*/
  }

  override def receive: Actor.Receive = {

    case JobsStop =>
      println ("stop:" + job)
      t2.cancel()
      self ! PoisonPill
  }

  private def format(command: String): ProcessBuilder = {
    command
  }
}

object Worker {
  def apply(sender:ActorRef, job:Job): Props = Props(classOf[Worker], sender, job)
}