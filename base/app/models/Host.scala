package models

import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.MongoConnection
import play.api.PlayException
import play.api.Play.current
import mongoContext._
import scala.util.Try

case class Host(name: String,
                hostGroupId: String,
                ip: String,
                active: Boolean,
                description: String,
                _id: ObjectId = new ObjectId)

case class AgentInterface(name:Option[String], port: Int)

object HostDAO extends SalatDAO[Host, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("host"))

object Host {

  def all(): List[Host] = HostDAO.find(MongoDBObject.empty).toList

  def create(e: Host) = {
    HostDAO.insert(e)
  }

  def edit(e: Host) = {
    delete(e._id.toString)
    create(e)
  }

  def get(id: String): Option[Host] = {
    Try(HostDAO.findOneById(new ObjectId(id))) getOrElse None
  }

  def delete(id: String) {
    HostDAO.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }
}