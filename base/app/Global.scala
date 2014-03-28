import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import models.sheduler.BaseActor
import play.api._

object Global extends GlobalSettings {

  val baseSystem = ActorSystem("baseSystem", ConfigFactory.load("base"))
  val baseActor = baseSystem.actorOf(Props[BaseActor], "base")

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    baseSystem.shutdown()
  }

}