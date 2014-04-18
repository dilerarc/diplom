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

  def clean(itemId: String, date: DateTime):Unit = {
    MonitoringDataDAO.remove(MongoDBObject("itemId" -> itemId) ++ ("date" $lt date))
  }

  def all(): List[MonitoringData] = MonitoringDataDAO.find(MongoDBObject.empty).toList

  def find(itemId: String, from:DateTime, to:DateTime): String = {
    grater[MonitoringDataCompressed]
      .toPrettyJSONArray(MonitoringDataDAO
      .find(MongoDBObject("itemId" -> itemId) ++ ("date" $gte from $lte to))
      .sort(orderBy = MongoDBObject("date" -> 0))
      .map(m => MonitoringDataCompressed(m.data, m.date.getMillis)).toTraversable)
  }

  def find(itemId: String, from:DateTime, to:DateTime, value:String, compare:CompareType.Value) = {
    MonitoringDataDAO.find(MongoDBObject("itemId" -> itemId) ++ ("date" $gte from $lte to) ++ {compare match {
      case CompareType.Eq => MongoDBObject("data" -> value)
      case CompareType.G => "data" $gt value
      case CompareType.L => "data" $lt value
      case CompareType.Ge => "data" $gte value
      case CompareType.Le => "data" $lte value
    }}).toList
  }

  def create(e: MonitoringData) = {
    MonitoringDataDAO.insert(e)
  }
}