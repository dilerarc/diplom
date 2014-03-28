package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object HostGroupController extends Controller {

  val form = Form(
    "name" -> nonEmptyText
  )

  def all = Action {
    Ok(views.html.host_group.all(HostGroup.all()))
  }

  def showNew = Action {
    Ok(views.html.host_group.create(form))
  }

  def edit(id: String) = Action {
    HostGroup.get(id)
      .fold(Redirect(routes.HostGroupController.all))(
        entity => Ok(views.html.host_group.edit(entity, form.fill(entity.name))))
  }

  def saveNew = Action {
    implicit request =>
      form.bindFromRequest.fold(
        errors => BadRequest(views.html.host_group.create(errors)),
        name => {
          HostGroup.create(HostGroup(name))
          Redirect(routes.HostGroupController.all)
        }
      )
  }

  def save(id: String) = Action {
    implicit request =>
      HostGroup.get(id)
        .fold(Redirect(routes.HostGroupController.all))(
          entity =>
            form.bindFromRequest.fold(
              errors => BadRequest(views.html.host_group.edit(entity, errors)),
              name => {
                HostGroup.edit(entity.copy(name = name))
                Redirect(routes.HostGroupController.all)
              }
            )
        )
  }
}