package models.core

import scala.concurrent.duration._
import akka.actor._
import com.ucheck.common.{Job, JobsStop, JobResult}
import scala.util.Random
import org.joda.time.DateTime
import scala.sys.process.ProcessBuilder
import play.api.Logger
import com.ucheck.agent._
import com.ucheck.common.JobsStop
import com.ucheck.common.Job
import com.ucheck.common.JobResult

class Worker(sender: ActorRef, job: Job) extends Actor {

  import context.dispatcher

  var t2 = context.system.scheduler.schedule(1 seconds, job.updateInterval seconds) {

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
    context.setReceiveTimeout(20 seconds)
    Logger.info("Simple worker started.")
  }

  override def postStop(): Unit = {
    Logger.info("Simple worker stopped.")
  }

  override def receive: Actor.Receive = {
    case JobsStop =>
      Logger.info(s"Received jobs stop. Actor: $self.")

      t2.cancel()
      self ! PoisonPill
  }
}


object Worker {
  def apply(sender:ActorRef, job:Job): Props = Props(classOf[Worker], sender, job)
}
