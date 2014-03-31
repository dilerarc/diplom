package com.ucheck.common

case class Job(itemId:String, command:String, updateInterval:Long)
case class Jobs(jobs:Set[Job])
case class JobResult(itemId:String, data:String)
case class JobsStop(host:String)
case object KeepWorking