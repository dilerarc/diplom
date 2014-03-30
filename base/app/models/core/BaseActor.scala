package models.core

import akka.actor._
import models._
import scala.concurrent.duration._
import com.ucheck.agent.Agent
import com.ucheck.common.{JobStop, Job, JobResult}
import play.api.Play.current

class BaseActor extends Actor {

  import context.dispatcher

  override def preStart(): Unit = {
    println("preStart")
    context.system.scheduler.schedule(0 seconds, 5 seconds) {
      refresh()
    }
  }

  def receive = {
    case result: JobResult => MonitoringData.create(MonitoringData(result.itemId, result.data))
  }

  private def refresh() = {

    context.actorSelection("akka.tcp://agentSystem*") ! JobStop

    Item.all()
      .filter(_.active)
      .filter(_.itemType == ItemType.Agent)
      .filter(item ⇒ Host.get(item.hostId).get.active)
      .foreach(
        item ⇒ {

          val itemId = item._id.toString
          val host = Host.get(item.hostId).get.name
          val command = ShellCommand.get(item.commandId).get.command
          val updateInterval = item.updateInterval

          val actor = context.system.actorOf(Agent(self, Job(itemId, command, updateInterval)), s"agent:$host:$itemId")
          context watch actor
        }
      )
  }
}