package controllers

import play.api._
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
      "compareType" -> nonEmptyText
    ) {
      (name, itemId, value, compareType) =>
        Trigger(name, itemId, value, CompareType.withName(compareType))
    } {
      trigger => Some(trigger.name, trigger.itemId, trigger.value, trigger.compareType.toString)
    }
  )

  def all = Action {
    Ok(views.html.trigger.all(Trigger.all()))
  }

  def showNew = Action {
    Ok(views.html.trigger.create(form))
  }

  def edit(id: String) = Action {
    Trigger.get(id)
      .fold(Redirect(routes.TriggerController.all))(
        entity => Ok(views.html.trigger.edit(entity, form.fill(entity))))
  }

  def saveNew = Action {
    implicit request =>
      form.bindFromRequest.fold(
        errors => BadRequest(views.html.trigger.create(errors)),
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
              errors => BadRequest(views.html.trigger.edit(entity, errors)),
              command => {
                Trigger.edit(command.copy(_id = entity._id))
                Redirect(routes.TriggerController.all)
              }
            )
        )
  }
}