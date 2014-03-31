package com.ucheck.agent

import akka.actor._
import com.ucheck.common.Jobs
import com.ucheck.common.JobsStop
import com.ucheck.common.Jobs
import scala.concurrent.duration._

class Agent extends Actor {

  var workers: Set[ActorRef] = Set()
  context.setReceiveTimeout(20 seconds)

  override def preStart(): Unit = {
    println("preStart")
  }

  override def receive: Actor.Receive = {

    case jobs: Jobs =>

      workers foreach (worker => {
        worker ! JobsStop
        workers = Set()
      })

      jobs.jobs.foreach(job => {
        val worker = context.actorOf(Worker(sender, job))
        workers += worker
      })

    case JobsStop => context.children foreach (worker => {
      println(worker)
      worker ! JobsStop
      workers = Set()
    })

    case ReceiveTimeout ⇒
      context.setReceiveTimeout(Duration.Undefined)
      println("timout")

      workers foreach (worker => {
        worker ! JobsStop
        workers = Set()
      })
  }
}