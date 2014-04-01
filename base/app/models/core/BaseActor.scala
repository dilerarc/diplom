package models.core

import akka.actor._
import models._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop, Job, JobResult}

class BaseActor extends Actor {

  import context.dispatcher

  var task: Cancellable = null

  override def preStart(): Unit = {
    task = context.system.scheduler.schedule(0 seconds, 15 seconds) {
      refresh()
    }
  }

  def receive = {
    case jobsStop: JobsStop =>
      val ip = Host.get(jobsStop.host).get.ip
      context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/agentActor") ! jobsStop

    case result: JobResult =>
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
      .filter(_.itemType == ItemType.Agent)
      .filter(item => hosts.contains(item.hostId))
      .groupBy(_.hostId)

    items.foreach(
      pair => {
        val ip = hosts(pair._1).ip
        val jobs = pair._2.map(item => Job(item._id.toString, commands(item.commandId).command, item.updateInterval))
        context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/agentActor") ! Jobs(jobs)
      })

  }
}