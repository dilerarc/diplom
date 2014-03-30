import akka.actor.{Props, ActorSystem}
import com.novus.salat.{TypeHintFrequency, StringTypeHintStrategy, Context}
import com.typesafe.config.ConfigFactory
import models.core.BaseActor
import play.api.Play
import play.api.Play.current

package object mongoContext {
  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)
    context
  }

  val baseSystem = ActorSystem("baseSystem", ConfigFactory.load("base"))
  implicit val baseActor = baseSystem.actorOf(Props[BaseActor], "base")
}