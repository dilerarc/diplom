package com.ucheck.agent

import akka.actor._
import com.ucheck.common.{AgentJobs, JobsStopAll, JobsStop, Jobs}
import scala.concurrent.duration._

class Manager extends Actor {

  var workers: Set[ActorRef] = Set()

  context.setReceiveTimeout(20.seconds)

  override def preStart(): Unit = {
    println("preStart")
  }

  override def receive: Actor.Receive = {

    case AgentJobs(jobs) =>

      println(jobs)
      stop()

      jobs.foreach(job => {
        val worker = context.actorOf(Worker(sender, job))
        workers += worker
      })

    case js: JobsStop =>
      workers foreach (_ ! js)

    case jsa: JobsStopAll =>
      workers foreach (_ ! jsa)

    case ReceiveTimeout ⇒
      println("timeout")
      stop()
  }

  private def stop() {
    workers foreach (worker => {
      worker ! JobsStopAll
    })
    workers = Set()
  }
}