package models

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{ModelCompanion, SalatDAO}
import com.novus.salat.grater
import com.mongodb.casbah.MongoConnection
import play.api.PlayException
import play.api.Play.current
import mongoContext._

case class MonitoringData(itemId: String, data: String, _id: ObjectId = new ObjectId)

object MonitoringDataDAO extends SalatDAO[MonitoringData, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("monitoring_data"))

object MonitoringData {

  def all(): List[MonitoringData] = MonitoringDataDAO.find(MongoDBObject.empty).toList

  def find(itemId: String): String = {
    grater[MonitoringData].toPrettyJSONArray(MonitoringDataDAO.find(MongoDBObject("itemId" -> itemId)).toTraversable)
  }

  def create(e: MonitoringData) = {
    MonitoringDataDAO.insert(e)
  }



}