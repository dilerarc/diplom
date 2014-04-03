package models.core

import akka.actor._
import models._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop, Job, JobResult}

class BaseActor extends Actor {

  import context.dispatcher

  var task: Cancellable = null
  var simpleActor: ActorRef = null

  override def preStart(): Unit = {
    simpleActor = context.actorOf(SimpleActor(), "simpleActor")

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
      .filter(item => hosts.contains(item.hostId))
      .groupBy(_.hostId)

    items.foreach(
      pair => {

        val ip = hosts(pair._1).ip

        val agentJobs = pair._2
          .filter(_.itemType == ItemType.Agent)
          .map(item => Job(item._id.toString, commands(item.commandId).command, item.updateInterval))
        context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/agentActor") ! Jobs(agentJobs)

        val simpleJobs = pair._2
          .filter(_.itemType == ItemType.Simple)
          .map(item => Job(item._id.toString, commands(item.commandId).command, item.updateInterval))

        simpleActor ! Jobs(simpleJobs)
      })

  }
}