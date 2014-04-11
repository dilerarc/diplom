package models.core

import akka.actor._
import models._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop, Job, JobResult}
import play.api.Logger
import org.joda.time.DateTime

class Director extends Actor {

  import context.dispatcher

  context.system.scheduler.schedule(0 seconds, 15 seconds) {
    refreshJobs()
  }

  context.system.scheduler.schedule(5 seconds, 1 hours) {
    Item.all().foreach(
      item => {
        val date = DateTime.now().minusMinutes(item.keepPeriod.toInt)
        MonitoringData.clean(item._id.toString, date)
      })
  }

  val localManager = context.actorOf(Manager(), "localManager")

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
      MonitoringData.create(MonitoringData(result.itemId, result.data, result.date))

    case triggerCheckResult: TriggerCheckResult =>
      //send email

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
        context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/manager") ! Jobs(agentJobs)

        val simpleJobs = pair._2
          .filter(_.itemType == ItemType.Simple)
          .map(item => Job(item._id.toString, commands(item.commandId).command, item.updateInterval))

        Logger.info("Sending jobs to local manager")
        Logger.info(simpleJobs.toString())
        localManager ! Jobs(simpleJobs)

      })


      localManager ! Triggers(Trigger.all().filter(_.active).toSet)
  }
}