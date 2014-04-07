package com.ucheck.common

import scala.sys.process.{Process, ProcessBuilder}
import com.ucheck.common.Const._

object Util {

  def format(command: String): ProcessBuilder = {
    val list = command.split("\\|")
      .map(_.trim)
      .map(s => Process(argumentDelimiter.findAllIn(s)
      .map(_.replace("'", "")).toSeq))
    list.tail.foldLeft(list.head)(_ #| _)
  }

}
