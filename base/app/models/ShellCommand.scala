package models

import play.api.Play.current
import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.MongoConnection
import play.api.PlayException
import com.mongodb.casbah.commons.MongoDBObject
import mongoContext._
import scala.util.Try

case class ShellCommand(name:String, command: String, _id: ObjectId = new ObjectId)

object ShellCommandDAO extends SalatDAO[ShellCommand, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error", "Could not find mongodb.default.db in settings")))("shell_command"))

object ShellCommand {

  def all(): List[ShellCommand] = ShellCommandDAO.find(MongoDBObject.empty).toList

  def create(e: ShellCommand) = {
    ShellCommandDAO.insert(e)
  }

  def edit(e: ShellCommand) = {
    delete(e._id.toString)
    create(e)
  }

  def get(id: String): Option[ShellCommand] = {
    Try(ShellCommandDAO.findOneById(new ObjectId(id))) getOrElse None
  }

  def delete(id: String) {
    ShellCommandDAO.remove(MongoDBObject("_id" -> new ObjectId(id)))
  }
}