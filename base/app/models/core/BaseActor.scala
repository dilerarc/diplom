package models.core

import akka.actor._
import models._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop, Job, JobResult}
import java.util.UUID

class BaseActor extends Actor {

  import context.dispatcher

  var task:Cancellable = null
  //var jobs: Map[String, Job] = Map()

  override def preStart(): Unit = {
    println("preStart")
    println(context.system)
    task = context.system.scheduler.schedule(0 seconds, 15 seconds) {
      refresh()
    }
  }

  def receive = {
    case jobsStop: JobsStop =>
      val ip = Host.get(jobsStop.host).get.ip
      context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/agentActor") ! jobsStop

    case result: JobResult =>
      println(result)
      MonitoringData.create(MonitoringData(result.itemId, result.data))

    case ActorIdentity(id, actor)  => println(id)
      //actor.get ! jobs(id)
  }

  private def refresh() = {


    Item.all()
      .filter(_.active)
      .filter(_.itemType == ItemType.Agent)
      .filter(item ⇒ Host.get(item.hostId).get.active)
      .foreach(
        item ⇒ {

          val itemId = item._id.toString
          val ip = Host.get(item.hostId).get.ip
          val command = ShellCommand.get(item.commandId).get.command
          val updateInterval = item.updateInterval
          //val id = UUID.randomUUID().toString

          //jobs += id -> Job(itemId, command, updateInterval)
          context.system.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/agentActor") ! Jobs(Set(Job(itemId, command, updateInterval)))

          // as ! Identify

        }
      )
  }
}