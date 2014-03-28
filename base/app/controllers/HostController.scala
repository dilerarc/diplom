package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object HostController extends Controller {

  val form: Form[Host] = Form(
    mapping(
      "name" -> nonEmptyText,
      "hostGroupId" -> nonEmptyText,
      "ip" -> nonEmptyText,
      "active" -> optional(text),
      "description" -> nonEmptyText
    ) {
      (name, hostGroupId, ip, active, description) => Host(name, hostGroupId, ip, active exists (_.nonEmpty), description)
    } {
      host => Some(host.name, host.hostGroupId, host.ip, Option(if(host.active) "on" else ""), host.description)
    }
  )


  def all = Action {
    Ok(views.html.host.all(Host.all()))
  }

  def showNew = Action {
    Ok(views.html.host.create(HostGroup.all(), form))
  }

  def edit(id: String) = Action {
    Host.get(id)
      .fold(Redirect(routes.HostController.all))(
        entity => Ok(views.html.host.edit(entity, HostGroup.all(), form.fill(entity))))
  }

  def saveNew = Action {
    implicit request =>
      form.bindFromRequest.fold(
        errors => BadRequest(views.html.host.create(HostGroup.all(), errors)),
        host => {
          Host.create(host)
          Redirect(routes.HostController.all)
        }
      )
  }

  def save(id: String) = Action {
    implicit request =>
      Host.get(id)
        .fold(Redirect(routes.HostController.all))(
          entity =>
            form.bindFromRequest.fold(
              errors => BadRequest(views.html.host.edit(entity, HostGroup.all(), errors)),
              host => {
                Host.edit(host.copy(_id = entity._id))
                Redirect(routes.HostController.all)
              }
            )
        )
  }
}