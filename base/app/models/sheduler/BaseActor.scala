package models.sheduler

import akka.actor.{Identify, ActorIdentity, ActorRef, Actor}
import models.{ShellCommand, Host, ItemType, Item}
import com.ucheck.common.Job

class BaseActor extends Actor {

  var agents = Map[String, ActorRef]()

  override def preStart(): Unit = {
    println("preStart")
    refreshActors()
    refreshJobs()
  }

  def receive = {
    case ref: ActorRef if !(agents.values.toSet contains ref) ⇒
      agents += (ref.path.address.host.get -> ref)
      context watch ref
    case ActorIdentity(message, actor) if !actor.isEmpty && !agents.values.toSet.contains(actor.get) ⇒
      agents += (actor.get.path.address.host.get -> actor.get)
      context watch actor.get
  }

  def refreshJobs() = {

    val hosts = Item.all()
      .filter(i ⇒ i.active && i.itemType == ItemType.Agent)
      .groupBy(item ⇒ Host.get(item.hostId))
      .filter(host ⇒ host._1.get.active)

    hosts.foreach(
      pair ⇒ {
        val actor = agents(pair._1.get.ip)
        val items = pair._2
        items.foreach(
          item ⇒ actor ! Job(item._id.toString, ShellCommand.get(item.commandId).get.command, item.updateInterval)
        )
      }
    )
  }

  def refreshActors() = {
    Host.all()
      .filter(_.active).foreach(
        host => {
          val ip = host.ip
          val actor = context.actorSelection(s"akka.tcp://agentSystem@$ip:2552/user/agent")
          actor ! Identify
        }
      )
  }
}