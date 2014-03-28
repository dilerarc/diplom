package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object ChartController extends Controller {

  def show(itemId: String) = Action {
    Item.get(itemId)
      .fold(Redirect(routes.ItemController.all))(
        entity => Ok(views.html.chart.show(entity, MonitoringData.find(entity._id.toString))))
  }

}