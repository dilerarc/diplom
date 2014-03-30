package com.ucheck.common

case class Job(itemId:String, command:String, updateInterval:Long)
case class JobResult(itemId:String, data:String)
case class JobStop(itemId:String)
case object KeepWorking