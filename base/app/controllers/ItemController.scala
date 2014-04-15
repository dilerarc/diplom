package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object ItemController extends Controller {

  val form: Form[Item] = Form(
    mapping(
      "name" -> nonEmptyText,
      "hostId" -> nonEmptyText,
      "itemGroupId" -> nonEmptyText,
      "itemType" -> nonEmptyText,
      "commandId" -> nonEmptyText,
      "dataType" -> nonEmptyText,
      "units" -> nonEmptyText,
      "updateInterval" -> longNumber(0, 100),
      "keepPeriod" -> longNumber(0, 100),
      "description" -> nonEmptyText,
      "active" -> optional(text)
    ) {
      (name, hostId, itemGroupId, itemType, commandId, dataType, units, updateInterval, keepPeriod, description, active) =>
        Item(name, hostId, itemGroupId, ItemType.withName(itemType), commandId, DataType.withName(dataType), units, updateInterval, keepPeriod, description, active exists (_.nonEmpty))
    } {
      item => Some(item.name, item.hostId, item.itemGroupId, item.itemType.toString, item.commandId,
        item.dataType.toString, item.units, item.updateInterval, item.keepPeriod, item.description, Option(if (item.active) "on" else ""))
    }
  )


  def all = Action {
    Ok(views.html.item.all(Item.all()))
  }

  def showNew = Action {
    Ok(views.html.item.create(Host.all(), ItemGroup.all(), ItemType.values.toSeq, DataType.values.toSeq, ShellCommand.all(), form))
  }

  def edit(id: String) = Action {
    Item.get(id)
      .fold(Redirect(routes.ItemController.all))(
        entity => Ok(views.html.item.edit(entity, Host.all(), ItemGroup.all(), ItemType.values.toSeq, DataType.values.toSeq, ShellCommand.all(), form.fill(entity))))
  }

  def saveNew = Action {
    implicit request =>
      form.bindFromRequest.fold(
        errors => BadRequest(views.html.item.create(Host.all(), ItemGroup.all(), ItemType.values.toSeq, DataType.values.toSeq, ShellCommand.all(), errors)),
        item => {
          Item.create(item)
          Redirect(routes.ItemController.all)
        }
      )
  }

  def save(id: String) = Action {
    implicit request =>
      Item.get(id)
        .fold(Redirect(routes.ItemController.all))(
          entity =>
            form.bindFromRequest.fold(
              errors => BadRequest(views.html.item.edit(entity, Host.all(), ItemGroup.all(), ItemType.values.toSeq, DataType.values.toSeq, ShellCommand.all(), errors)),
              item => {
                Item.edit(item.copy(_id = entity._id))
                Redirect(routes.ItemController.all)
              }
            )
        )
  }
}