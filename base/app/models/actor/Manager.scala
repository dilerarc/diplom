package models.actor

import akka.actor._
import scala.concurrent.duration._
import com.ucheck.common._
import play.api.Logger
import scala.collection.mutable
import com.ucheck.common.JobsStop
import com.ucheck.common.JobsStopAll
import com.ucheck.common.SimpleJobs

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

    case SimpleJobs(jobs) =>
      Logger.info(s"Received simple jobs. Actor: $self, jobs: $jobs.")
      stop(simpleWorkers)

      jobs.foreach(job => {
        val worker = context.actorOf(Worker(sender, job))
        simpleWorkers += worker
      })

    case SNMPJobs(jobs) =>
      Logger.info(s"Received snmp jobs. Actor: $self, jobs: $jobs.")
      stop(snmpWorkers)

      jobs.foreach(job => {
        val worker = context.actorOf(Worker(sender, job))
        snmpWorkers += worker
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