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
import models.{ItemType, Item}
import scala.util.Try

class Worker(sender: ActorRef, job: Job) extends Actor {

  import context.dispatcher

  var t2 = context.system.scheduler.schedule(1.seconds, job.updateInterval.seconds) {

    val format1: ProcessBuilder = F.format(job.command)
    println(format1)
    val s = Try(format1.!!).getOrElse("")
    println(s)

    val res = Item.get(job.itemId).get.itemType match {
      case ItemType.Simple => if (s.contains("alive")) "1" else "0"
      case ItemType.SNMP => "\\d+".r.findFirstIn(s.split("\\s+").toList.reverse.head).get
    }

    sender ! JobResult(job.itemId, res, DateTime.now)
  }

  override def preStart(): Unit = {
    Logger.info("Worker started.")
  }

  override def postStop(): Unit = {
    Logger.info("Worker stopped.")
  }

  override def receive: Actor.Receive = {
    case JobsStop(host) => if (Item.get(job.itemId).get.hostId == host)
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
  def apply(sender: ActorRef, job: Job): Props = Props(classOf[Worker], sender, job)
}
