package models.core

import scala.concurrent.duration._
import akka.actor._
import com.ucheck.common.{Job, JobsStop, JobResult}
import scala.util.Random
import org.joda.time.DateTime
import scala.sys.process.ProcessBuilder

class SimpleWorker(sender: ActorRef, job: Job) extends Actor {

  import context.dispatcher

  var t2: Cancellable = null

  override def preStart(): Unit = {
    println("start:" + job)

    t2 = context.system.scheduler.schedule(1 seconds, job.updateInterval seconds) {
      //sender ! JobResult(job.itemId, format(job.command).!!, new Date())
      //fping -t 100 ya.ru
      sender ! JobResult(job.itemId, Random.nextLong().toString, DateTime.now)
    }
  }

  override def receive: Actor.Receive = {
    case JobsStop =>
      println("stop:" + job)
      t2.cancel()
      self ! PoisonPill
  }
}


object SimpleWorker {
  def apply(sender:ActorRef, job:Job): Props = Props(classOf[SimpleWorker], sender, job)
}
