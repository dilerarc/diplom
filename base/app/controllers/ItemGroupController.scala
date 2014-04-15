package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object ItemGroupController extends Controller {

  val form = Form(
    "name" -> nonEmptyText
  )

  def all = Action {
    Ok(views.html.item_group.all(ItemGroup.all()))
  }

  def showNew = Action {
    Ok(views.html.item_group.create(form))
  }

  def edit(id: String) = Action {
    ItemGroup.get(id)
      .fold(Redirect(routes.ItemGroupController.all))(
        entity => Ok(views.html.item_group.edit(entity, form.fill(entity.name))))
  }

  def saveNew = Action {
    implicit request =>
      form.bindFromRequest.fold(
        errors => BadRequest(views.html.item_group.create(errors)),
        name => {
          ItemGroup.create(ItemGroup(name))
          Redirect(routes.ItemGroupController.all)
        }
      )
  }

  def save(id: String) = Action {
    implicit request =>
      ItemGroup.get(id)
        .fold(Redirect(routes.ItemGroupController.all))(
          entity =>
            form.bindFromRequest.fold(
              errors => BadRequest(views.html.item_group.edit(entity, errors)),
              name => {
                ItemGroup.edit(entity.copy(name = name))
                Redirect(routes.ItemGroupController.all)
              }
            )
        )
  }
}