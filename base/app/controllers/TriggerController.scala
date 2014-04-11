package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object TriggerController extends Controller {

  val form: Form[Trigger] = Form(
    mapping(
      "name" -> nonEmptyText,
      "itemId" -> nonEmptyText,
      "value" -> nonEmptyText,
      "compareType" -> nonEmptyText,
      "active" -> optional(text)
    ) {
      (name, itemId, value, compareType, active) =>
        Trigger(name, itemId, value, CompareType.withName(compareType), active exists (_.nonEmpty))
    } {
      trigger => Some(trigger.name, trigger.itemId, trigger.value, trigger.compareType.toString, Option(if (trigger.active) "on" else ""))
    }
  )

  def all = Action {
    Ok(views.html.trigger.all(Trigger.all()))
  }

  def showNew = Action {
    Ok(views.html.trigger.create(CompareType.values.toSeq, Item.all(), form))
  }

  def edit(id: String) = Action {
    Trigger.get(id)
      .fold(Redirect(routes.TriggerController.all))(
        entity => Ok(views.html.trigger.edit(entity, CompareType.values.toSeq, Item.all(), form.fill(entity))))
  }

  def saveNew = Action {
    implicit request =>
      form.bindFromRequest.fold(
        errors => BadRequest(views.html.trigger.create(CompareType.values.toSeq, Item.all(), errors)),
        command => {
          Trigger.create(command)
          Redirect(routes.TriggerController.all)
        }
      )
  }

  def save(id: String) = Action {
    implicit request =>
      Trigger.get(id)
        .fold(Redirect(routes.TriggerController.all))(
          entity =>
            form.bindFromRequest.fold(
              errors => BadRequest(views.html.trigger.edit(entity, CompareType.values.toSeq, Item.all(), errors)),
              command => {
                Trigger.edit(command.copy(_id = entity._id))
                Redirect(routes.TriggerController.all)
              }
            )
        )
  }
}