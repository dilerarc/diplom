package models.core

import akka.actor._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop}

class SimpleActor extends Actor {


  var workers: Set[ActorRef] = Set()

  context.setReceiveTimeout(20 seconds)

  override def preStart(): Unit = {
    println("preStart")
  }

  def receive = {
    case jobs: Jobs =>

      println(jobs.jobs)
      workers foreach (worker => {
        worker ! JobsStop
      })
      workers = Set()

      jobs.jobs.foreach(job => {
        val worker = context.actorOf(SimpleWorker(sender, job))
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

object SimpleActor {
  def apply(): Props = Props(classOf[SimpleActor])
}