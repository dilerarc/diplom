package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object ApplicationController extends Controller {

  val taskForm = Form (
    "label" -> nonEmptyText
  )

  def sign = Action {
    Ok(views.html.sign())
  }

  def home = Action {
    Ok(views.html.sign())
  }

  def index = Action {
    Redirect(routes.ApplicationController.home)
  }

}