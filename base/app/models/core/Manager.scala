package models.core

import akka.actor._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop}
import play.api.Logger
import models.Triggers
import scala.collection.mutable

class Manager extends Actor {

  val simpleWorkers: mutable.Set[ActorRef] = mutable.Set.empty
  val snmpWorkers: mutable.Set[ActorRef] = mutable.Set.empty
  val triggerWorkers: mutable.Set[ActorRef] = mutable.Set.empty

  override def preStart(): Unit = {
    context.setReceiveTimeout(20 seconds)
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

    case JobsStop => stop(simpleWorkers)

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
      worker ! JobsStop
    })
    workers.clear()
  }
}

object Manager {
  def apply(): Props = Props(classOf[Manager])
}