package models.core

import akka.actor._
import scala.concurrent.duration._
import com.ucheck.common.Job

class SNMPActor extends Actor {

  import context.dispatcher

  var task: Cancellable = null

  override def preStart(): Unit = {

  }

  def receive = {
    case _ => println ("hui")
  }

}