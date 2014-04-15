package models

import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.MongoConnection
import play.api.PlayException
import play.api.Play.current
import mongoContext._
import com.mongodb.casbah.commons.MongoDBObject
import scala.util.Try

case class ItemGroup(name: String,
                     _id: ObjectId = new ObjectId)

object ItemGroupDAO extends SalatDAO[ItemGroup, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("item_group"))

object ItemGroup {

  def all(): List[ItemGroup] = ItemGroupDAO.find(MongoDBObject.empty).toList

  def create(e: ItemGroup) = {
    ItemGroupDAO.insert(e)
  }

  def edit(e: ItemGroup) = {
    delete(e._id.toString)
    create(e)
  }

  def get(id: String): Option[ItemGroup] = {
    Try(ItemGroupDAO.findOneById(new ObjectId(id))) getOrElse None
  }

  def delete(id: String) {
    ItemGroupDAO.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }
}