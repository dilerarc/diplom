package controllers

import play.api.mvc._

import models._
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
import scala.util.Try

object ChartController extends Controller {

  def show(itemId: String) = Action {
    Item.get(itemId)
      .fold(Redirect(routes.ItemController.all))(
        entity => Ok(views.html.chart.show(entity)))
  }

  def showMulti(itemGroupId: String) = Action {
    Ok(views.html.chart.showMulti(Item.getByItemGroup(itemGroupId)))
  }

  def get(itemId: String, period: String) = Action {
    var m = Try(period.toInt).getOrElse(5)
    if(m < 0) m = 5
    Item.get(itemId)
      .fold(Ok("ERROR"))(
        entity => {
          val to = DateTime.now
          val from = to - m.minutes
          Ok(MonitoringData.find(entity._id.toString, from, to))
        })
  }
}