package com.ucheck.agent

import scala.concurrent.duration._
import akka.actor._
import com.ucheck.common.{JobsStopAll, JobsStop, JobResult, Job}
import scala.sys.process._
import org.joda.time.DateTime

class Worker(sender: ActorRef, job: Job) extends Actor {

  import context.dispatcher

  var t2: Cancellable = null

  override def preStart(): Unit = {
    println("start:" + job)
    t2 = context.system.scheduler.schedule(1.seconds, job.updateInterval.seconds) {
      //sudo atop 0 1 -PPRM | grep java | awk '{print $12}'
      val format1: ProcessBuilder = F.format(job.command)
      println(format1)
      val s = format1.!!
      val r = "\\d+".r
      val result = r.findFirstIn(s).get
      println(result)
      sender ! JobResult(job.itemId, result, DateTime.now)
    }
  }

  override def receive: Actor.Receive = {

    case JobsStopAll =>
      println("stop:" + job)
      t2.cancel()
      self ! PoisonPill
  }
}

object Worker {
  def apply(sender: ActorRef, job: Job): Props = Props(classOf[Worker], sender, job)
}