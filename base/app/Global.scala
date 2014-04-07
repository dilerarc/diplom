import akka.actor.{Props}
import models.core.Director
import play.api._
import play.api.libs.concurrent.Akka
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Akka.system.actorOf(Props[Director], "baseActor")
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}