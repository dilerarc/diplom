package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object ShellCommandController extends Controller {

  val form: Form[ShellCommand] = Form(
    mapping(
      "name" -> nonEmptyText,
      "command" -> nonEmptyText
    ) {
      (name, command) =>
        ShellCommand(name, command)
    } {
      item => Some(item.name, item.command)
    }
  )

  def all = Action {
    Ok(views.html.shell_command.all(ShellCommand.all()))
  }

  def showNew = Action {
    Ok(views.html.shell_command.create(form))
  }

  def edit(id: String) = Action {
    ShellCommand.get(id)
      .fold(Redirect(routes.ShellCommandController.all))(
        entity => Ok(views.html.shell_command.edit(entity, form.fill(entity))))
  }

  def saveNew = Action {
    implicit request =>
      form.bindFromRequest.fold(
        errors => BadRequest(views.html.shell_command.create(errors)),
        command => {
          ShellCommand.create(command)
          Redirect(routes.ShellCommandController.all)
        }
      )
  }

  def save(id: String) = Action {
    implicit request =>
      ShellCommand.get(id)
        .fold(Redirect(routes.ShellCommandController.all))(
          entity =>
            form.bindFromRequest.fold(
              errors => BadRequest(views.html.shell_command.edit(entity, errors)),
              command => {
                ShellCommand.edit(command.copy(_id = entity._id))
                Redirect(routes.ShellCommandController.all)
              }
            )
        )
  }
}