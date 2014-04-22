package models.actor

import akka.actor._
import scala.concurrent.duration._
import com.ucheck.common._
import play.api.Logger
import scala.collection.mutable
import models.{Trigger, TriggerCheckResult, Triggers}
import com.ucheck.common.JobsStop
import com.ucheck.common.JobsStopAll
import models.util.EmailSender
import play.api.i18n.Messages
import org.joda.time.DateTime

class TriggerManager extends Actor {

  val workers: mutable.Set[ActorRef] = mutable.Set.empty

  import context.dispatcher
  context.system.scheduler.schedule(0.seconds, 1.hours) {
    resetTriggers()
  }

  override def preStart(): Unit = {
    context.setReceiveTimeout(20.seconds)
    Logger.info("Trigger manager started.")
  }

  override def postStop(): Unit = {
    Logger.info("Trigger manager stopped.")
  }

  def receive = {

    case js: JobsStop =>
      Logger.info(s"Received jobs stop. Actor: $self.")
      workers foreach (_ ! js)

    case jsa: JobsStopAll =>
      Logger.info(s"Received all jobs stop. Actor: $self.")
      workers foreach (_ ! jsa)

    case triggers: Triggers =>
      Logger.info(s"Received triggers. Actor: $self, triggers: $triggers.")
      stop(workers)

      triggers.triggers.foreach(trigger => {
        val worker = context.actorOf(TriggerWorker(sender, trigger))
        workers += worker
      })

    case ReceiveTimeout ⇒
      Logger.info(s"Received timeout. Actor: $self.")
      stop(workers)

    case Triggers(triggers) =>
      Logger.info(s"Received triggers. Actor: $self, triggers: $triggers.")
      stop(workers)

      triggers.foreach(trigger => {
        val triggerWorker = context.actorOf(TriggerWorker(self, trigger))
        workers += triggerWorker
      })

    case TriggerCheckResult(triggerId, itemId, data) =>
      Logger.info(s"Received trigger check result. Actor: $self.")
      val report = s"Item: $itemId, data: [${data.toString()}]"
      val trigger = Trigger.get(triggerId).get
      if (!trigger.processed) {
        EmailSender.sendEmail(Email(
          Messages("contact.request.subject", DateTime.now()),
          Messages("contact.request.email.recipient"),
          Messages("contact.request.email.recipient", report)))
        Trigger.edit(trigger.copy(_id = trigger._id, processed = true))
      }
  }

  private def stop(workers: mutable.Set[ActorRef]) {
    workers foreach (worker => {
      worker ! JobsStopAll
    })
    workers.clear()
  }

  private def resetTriggers() = {
    Trigger.all() foreach (
      trigger => Trigger.edit(trigger.copy(_id = trigger._id, processed = false))
      )
  }

}

object TriggerManager {
  def apply(): Props = Props(classOf[TriggerManager])
}

