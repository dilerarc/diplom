package models.core

import akka.actor._
import models._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop, Job, JobResult}
import play.api.Logger

class BaseActor extends Actor {

  import context.dispatcher

  var task = context.system.scheduler.schedule(0 seconds, 15 seconds) {
    refresh()
  }
  var simpleActor = context.actorOf(SimpleActor(), "simpleActor")

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
      context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/agentActor") ! jobsStop

    case result: JobResult =>
      Logger.info(s"Received job result. Actor: $self, result: $result.")

      MonitoringData.create(MonitoringData(result.itemId, result.data, result.date))

  }

  private def refresh() = {

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
        Logger.info("Senting jobs to remote agents")
        Logger.info(agentJobs.toString())
        context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/agentActor") ! Jobs(agentJobs)

        val simpleJobs = pair._2
          .filter(_.itemType == ItemType.Simple)
          .map(item => Job(item._id.toString, commands(item.commandId).command, item.updateInterval))

        Logger.info("Senting jobs to local agents")
        Logger.info(simpleJobs.toString())
        simpleActor ! Jobs(simpleJobs)
      })

  }
}