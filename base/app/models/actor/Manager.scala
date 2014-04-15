package models.actor

import akka.actor._
import scala.concurrent.duration._
import com.ucheck.common.{JobsStopAll, Jobs, JobsStop}
import play.api.Logger
import models.Triggers
import scala.collection.mutable

class Manager extends Actor {

  val simpleWorkers: mutable.Set[ActorRef] = mutable.Set.empty
  val snmpWorkers: mutable.Set[ActorRef] = mutable.Set.empty
  val triggerWorkers: mutable.Set[ActorRef] = mutable.Set.empty

  override def preStart(): Unit = {
    context.setReceiveTimeout(20.seconds)
    Logger.info("Local manager started.")
  }

  override def postStop(): Unit = {
    Logger.info("Local manager stopped.")
  }

  def receive = {
    case jobs: Jobs =>
      Logger.info(s"Received jobs. Actor: $self, jobs: $jobs.")
      stop(simpleWorkers)

      jobs.jobs.foreach(job => {
        val worker = context.actorOf(Worker(sender, job))
        simpleWorkers += worker
      })

    case js: JobsStop =>
      Logger.info(s"Received jobs stop. Actor: $self.")
      simpleWorkers foreach (_ ! js)
      snmpWorkers foreach (_ ! js)
      triggerWorkers foreach (_ ! js)

    case jsa: JobsStopAll =>
      Logger.info(s"Received all jobs stop. Actor: $self.")
      simpleWorkers foreach (_ ! jsa)
      snmpWorkers foreach (_ ! jsa)
      triggerWorkers foreach (_ ! jsa)

    case triggers: Triggers =>
      Logger.info(s"Received triggers. Actor: $self, triggers: $triggers.")
      stop(triggerWorkers)

      triggers.triggers.foreach(trigger => {
        val triggerWorker = context.actorOf(TriggerWorker(sender, trigger))
        triggerWorkers += triggerWorker
      })

    case ReceiveTimeout ⇒
      Logger.info(s"Received timeout. Actor: $self.")
      stop(simpleWorkers)
      stop(snmpWorkers)
      stop(triggerWorkers)
  }

  private def stop(workers: mutable.Set[ActorRef]) {
    workers foreach (worker => {
      worker ! JobsStopAll
    })
    workers.clear()
  }
}

object Manager {
  def apply(): Props = Props(classOf[Manager])
}