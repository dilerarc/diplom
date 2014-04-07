package com.ucheck.agent

import akka.actor._
import com.ucheck.common.{JobsStop, Jobs}
import scala.concurrent.duration._

class Manager extends Actor {

  var workers: Set[ActorRef] = Set()

  context.setReceiveTimeout(20 seconds)

  override def preStart(): Unit = {
    println("preStart")
  }

  override def receive: Actor.Receive = {

    case jobs: Jobs =>

      println(jobs.jobs)
      workers foreach (worker => {
        worker ! JobsStop
      })
      workers = Set()

      jobs.jobs.foreach(job => {
        val worker = context.actorOf(Worker(sender, job))
        workers += worker
      })

    case JobsStop =>
      workers foreach (worker => {
        println(worker)
        worker ! JobsStop
      })
      workers = Set()


    case ReceiveTimeout ⇒
      println("timout")

      workers foreach (worker => {
        worker ! JobsStop
      })
      workers = Set()
  }
}