package models

import org.bson.types.ObjectId
import play.api.PlayException
import com.mongodb.casbah.MongoConnection
import com.novus.salat.dao.SalatDAO
import play.api.Play.current
import com.mongodb.casbah.commons.MongoDBObject
import scala.util.Try
import mongoContext._
import models.ItemType.ItemType
import models.DataType.DataType

case class Item(name: String,
                hostId: String,
                itemType: ItemType,
                commandId: String,
                dataType: DataType,
                units: String,
                updateInterval: Long,
                keepPeriod: Long,
                description: String,
                active: Boolean,
                _id: ObjectId = new ObjectId)

object ItemDAO extends SalatDAO[Item, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("item"))


object Item {

  def all(): List[Item] = ItemDAO.find(MongoDBObject.empty).toList

  def create(e: Item) = {
    ItemDAO.insert(e)
  }

  def edit(e: Item) = {
    delete(e._id.toString)
    create(e)
  }

  def get(id: String): Option[Item] = {
    Try(ItemDAO.findOneById(new ObjectId(id))) getOrElse None
  }

  def delete(id: String) {
    ItemDAO.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }
}

object ItemType extends Enumeration {

  type ItemType = Value

  val Simple = Value("Simple")
  val Agent = Value("Agent")
  val SNMP = Value("SNMP")
}

object DataType extends Enumeration {

  type DataType = Value

  val Number = Value("Number")
  val String = Value("String")
  val Boolean = Value("Boolean")
}