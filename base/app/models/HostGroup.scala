package models

import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.MongoConnection
import play.api.PlayException
import play.api.Play.current
import mongoContext._
import com.mongodb.casbah.commons.MongoDBObject
import scala.util.Try

case class HostGroup(name: String,
                     _id: ObjectId = new ObjectId)

object HostGroupDAO extends SalatDAO[HostGroup, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("host_group"))

object HostGroup {

  def all(): List[HostGroup] = HostGroupDAO.find(MongoDBObject.empty).toList

  def create(e: HostGroup) = {
    HostGroupDAO.insert(e)
  }

  def edit(e: HostGroup) = {
    delete(e._id.toString)
    create(e)
  }

  def get(id: String): Option[HostGroup] = {
    Try(HostGroupDAO.findOneById(new ObjectId(id))) getOrElse None
  }

  def delete(id: String) {
    HostGroupDAO.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }
}