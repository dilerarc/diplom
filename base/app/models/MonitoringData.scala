package models

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.novus.salat.grater
import com.mongodb.casbah.MongoConnection
import play.api.PlayException
import play.api.Play.current
import mongoContext._
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

case class MonitoringData(itemId: String, data: String, date:DateTime, _id: ObjectId = new ObjectId)
case class MonitoringDataCompressed(data: String, date:Long)

object MonitoringDataDAO extends SalatDAO[MonitoringData, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("monitoring_data"))

object MonitoringData {

  def all(): List[MonitoringData] = MonitoringDataDAO.find(MongoDBObject.empty).toList

  def find(itemId: String, from:DateTime, to:DateTime): String = {
    grater[MonitoringDataCompressed]
      .toPrettyJSONArray(MonitoringDataDAO.find(MongoDBObject("itemId" -> itemId) ++ ("date" $gte from $lte to))
      .map(m => MonitoringDataCompressed(m.data, m.date.getMillis)).toTraversable)
  }

  def create(e: MonitoringData) = {
    MonitoringDataDAO.insert(e)
  }



}