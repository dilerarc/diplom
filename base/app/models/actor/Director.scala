package models.actor

import akka.actor._
import models._
import scala.concurrent.duration._
import com.ucheck.common._
import play.api.Logger
import models.Triggers
import com.ucheck.common.JobsStop
import com.ucheck.common.Job
import com.ucheck.common.JobResult

class Director extends Actor {

  import context.dispatcher

  context.system.scheduler.schedule(0.seconds, 15.seconds) {
    refreshJobs()
  }

  val localManager = context.actorOf(Manager(), "localManager")
  val statsHandler = context.actorOf(StatsHandler(), "statsHandler")
  val triggerManager = context.actorOf(StatsHandler(), "triggerManager")

  override def preStart(): Unit = {
    Logger.info("Base actor started.")
  }

  override def postStop(): Unit = {
    Logger.info("Base actor stopped.")
  }

  def receive = {
    case jobsStop: JobsStop =>
      Logger.info(s"Received jobs stop. Actor: $self.")
      val ip = Host.get(jobsStop.host).get.ip
      context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/manager") ! jobsStop

    case result: JobResult =>
      Logger.info(s"Received job result. Actor: $self, result: $result.")
      statsHandler ! result
  }

  private def refreshJobs() = {

    val hosts = Host.all()
      .filter(_.active)
      .map(host => (host._id.toString, host))
      .toMap

    val commands = ShellCommand.all()
      .map(command => (command._id.toString, command))
      .toMap

    val items = Item.all()
      .filter(_.active)
      .filter(item => hosts.contains(item.hostId))
      .groupBy(_.hostId)

    items.foreach(
      pair => {

        val ip = hosts(pair._1).ip

        val agentJobs = pair._2
          .filter(_.itemType == ItemType.Agent)
          .map(item => Job(item._id.toString, commands(item.commandId).command, item.updateInterval))

        Logger.info("Sending jobs to remote manager")
        Logger.info(agentJobs.toString())
        context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/manager") ! AgentJobs(agentJobs)

        val simpleJobs = pair._2
          .filter(_.itemType == ItemType.Simple)
          .map(item => Job(item._id.toString, commands(item.commandId).command, item.updateInterval))

        Logger.info("Sending simple jobs to local manager")
        Logger.info(simpleJobs.toString())
        localManager ! SimpleJobs(simpleJobs)

        val snmpJobs = pair._2
          .filter(_.itemType == ItemType.SNMP)
          .map(item => Job(item._id.toString, commands(item.commandId).command, item.updateInterval))

        Logger.info("Sending snmp jobs to local manager")
        Logger.info(snmpJobs.toString())
        localManager ! SNMPJobs(snmpJobs)

      })


      triggerManager ! Triggers(Trigger.all().filter(_.active).toSet)
  }
}