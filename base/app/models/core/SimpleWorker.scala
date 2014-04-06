package models.core

import scala.concurrent.duration._
import akka.actor._
import com.ucheck.common.{Job, JobsStop, JobResult}
import scala.util.Random
import org.joda.time.DateTime
import scala.sys.process.ProcessBuilder
import play.api.Logger

class SimpleWorker(sender: ActorRef, job: Job) extends Actor {

  import context.dispatcher

  var t2 = context.system.scheduler.schedule(1 seconds, job.updateInterval seconds) {
    //sender ! JobResult(job.itemId, format(job.command).!!, new Date())
    //fping -t 100 ya.ru
    sender ! JobResult(job.itemId, Random.nextLong().toString, DateTime.now)
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


object SimpleWorker {
  def apply(sender:ActorRef, job:Job): Props = Props(classOf[SimpleWorker], sender, job)
}
