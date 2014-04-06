package models.core

import akka.actor._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop}
import play.api.Logger

class SimpleActor extends Actor {

  var workers: Set[ActorRef] = Set()

  override def preStart(): Unit = {
    context.setReceiveTimeout(20 seconds)
    Logger.info("Simple actor started.")
  }

  override def postStop(): Unit = {
    Logger.info("Simple actor stopped.")
  }

  def receive = {
    case jobs: Jobs =>

      Logger.info(s"Received jobs. Actor: $self, jobs: $jobs.")

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
      Logger.info(s"Received timeout. Actor: $self.")

      workers foreach (worker => {
        worker ! JobsStop
      })
      workers = Set()
  }
}

object SimpleActor {
  def apply(): Props = Props(classOf[SimpleActor])
}