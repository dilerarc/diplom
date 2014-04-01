package models

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.novus.salat.grater
import com.mongodb.casbah.MongoConnection
import play.api.PlayException
import play.api.Play.current
import mongoContext._
import java.util.Date
import com.mongodb.casbah.Imports._

case class MonitoringData(itemId: String, data: String, date:Date, _id: ObjectId = new ObjectId)
case class MonitoringDataCompressed(data: String, date:Long)

object MonitoringDataDAO extends SalatDAO[MonitoringData, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("monitoring_data"))

object MonitoringData {

  def all(): List[MonitoringData] = MonitoringDataDAO.find(MongoDBObject.empty).toList

  def find(itemId: String): String = {
    grater[MonitoringDataCompressed]
      .toPrettyJSONArray(MonitoringDataDAO.find(MongoDBObject("itemId" -> itemId))
      .map(m => MonitoringDataCompressed(m.data, m.date.getTime)).toTraversable)
  }

  def find(itemId: String, date:Date): String = {
    grater[MonitoringDataCompressed]
      .toPrettyJSONArray(MonitoringDataDAO.find(MongoDBObject("itemId" -> itemId) ++ ("date" $gt date))
      .map(m => MonitoringDataCompressed(m.data, m.date.getTime)).toTraversable)
  }

  def create(e: MonitoringData) = {
    MonitoringDataDAO.insert(e)
  }



}