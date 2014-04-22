package models

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.MongoConnection
import play.api.Play._
import play.api.PlayException
import scala.util.Try
import mongoContext._

case class Trigger(name: String,
                   itemId: String,
                   value: String,
                   compareType: CompareType.Value,
                   active: Boolean,
                   processed: Boolean = false,
                   _id: ObjectId = new ObjectId)

case class Triggers(triggers: Set[Trigger])
case class TriggerCheckResult(triggerId:String, itemId:String, data:List[MonitoringData])

object TriggerDAO extends SalatDAO[Trigger, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("trigger"))

object Trigger {

  def all(): List[Trigger] = TriggerDAO.find(MongoDBObject.empty).toList

  def create(e: Trigger) = {
    TriggerDAO.insert(e)
  }

  def edit(e: Trigger) = {
    delete(e._id.toString)
    create(e)
  }

  def get(id: String): Option[Trigger] = {
    Try(TriggerDAO.findOneById(new ObjectId(id))) getOrElse None
  }

  def delete(id: String) {
    TriggerDAO.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }

}

object CompareType extends Enumeration {
  val Eq = Value("Equal")
  val Ge = Value("Greater or equal")
  val Le = Value("Lesser or equal")
  val G = Value("Greater")
  val L = Value("Lesser")
}