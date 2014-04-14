package models.actor

import scala.concurrent.duration._
import akka.actor._
import com.ucheck.common.JobsStop
import scala.util.Random
import org.joda.time.DateTime
import scala.sys.process.ProcessBuilder
import play.api.Logger
import com.ucheck.common.JobsStop
import com.ucheck.common.Job
import com.ucheck.common.JobResult
import models.{TriggerCheckResult, MonitoringData, Item, Trigger}

class TriggerWorker(sender: ActorRef, trigger: Trigger) extends Actor {

  import context.dispatcher

  var date:DateTime = DateTime.now()

  var t2 = context.system.scheduler.schedule(1 seconds, 10 seconds) {

    val res = MonitoringData.find(trigger.itemId, date , DateTime.now(), trigger.value, trigger.compareType)
    date = DateTime.now()
    if(res.nonEmpty) sender ! TriggerCheckResult(trigger.itemId, res)
  }

  override def preStart(): Unit = {
    context.setReceiveTimeout(20 seconds)
    Logger.info("Trigger worker started.")
  }

  override def postStop(): Unit = {
    Logger.info("Trigger worker stopped.")
  }

  override def receive: Actor.Receive = {
    case JobsStop =>
      Logger.info(s"Received jobs stop. Actor: $self.")

      t2.cancel()
      self ! PoisonPill
  }
}

object TriggerWorker {
  def apply(sender:ActorRef, trigger:Trigger): Props = Props(classOf[TriggerWorker], sender, trigger)
}



