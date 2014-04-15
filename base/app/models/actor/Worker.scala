package models.actor

import scala.concurrent.duration._
import akka.actor._
import com.ucheck.common._
import org.joda.time.DateTime
import scala.sys.process.ProcessBuilder
import play.api.Logger
import com.ucheck.common.JobsStop
import com.ucheck.common.Job
import com.ucheck.common.JobResult
import models.Item

class Worker(sender: ActorRef, job: Job) extends Actor {

  import context.dispatcher

  var t2 = context.system.scheduler.schedule(1.seconds, job.updateInterval.seconds) {

    val format1: ProcessBuilder = F.format(job.command)
    println(format1)
    val s = format1.!!
    println(s)

    val r = "\\d+".r
    val result = r.findFirstIn(s).get
    println(result)

    val res = if(result.contains("alive")) "1" else "0"

    sender ! JobResult(job.itemId, res, DateTime.now)
  }

  override def preStart(): Unit = {
    Logger.info("Worker started.")
  }

  override def postStop(): Unit = {
    Logger.info("Worker stopped.")
  }

  override def receive: Actor.Receive = {
    case JobsStop(host) => if(Item.get(job.itemId).get.hostId == host)
      Logger.info(s"Received jobs stop. Actor: $self.")

      t2.cancel()
      self ! PoisonPill

    case JobsStopAll =>
      Logger.info(s"Received all jobs stop. Actor: $self.")

      t2.cancel()
      self ! PoisonPill
  }
}

object Worker {
  def apply(sender:ActorRef, job:Job): Props = Props(classOf[Worker], sender, job)
}
