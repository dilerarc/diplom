package models.core

import akka.actor._
import models._
import scala.concurrent.duration._
import com.ucheck.common.{Jobs, JobsStop, Job, JobResult}
import play.api.Logger
import org.joda.time.DateTime

class StatsHandler extends Actor {

  import context.dispatcher

  context.system.scheduler.schedule(5 seconds, 1 hours) {
    Item.all().foreach(
      item => {
        val date = DateTime.now().minusMinutes(item.keepPeriod.toInt)
        MonitoringData.clean(item._id.toString, date)
      })
  }

  override def preStart(): Unit = {
    Logger.info("Stats handler started.")
  }

  override def postStop(): Unit = {
    Logger.info("Stats handler stopped.")
  }

  def receive = {
    case result: JobResult =>
      Logger.info(s"Received job result. Actor: $self, result: $result.")
      MonitoringData.create(MonitoringData(result.itemId, result.data, result.date))
  }
}

object StatsHandler {
  def apply(): Props = Props(classOf[StatsHandler])
}