package com.ucheck.common

import java.util.Date

case class Job(itemId:String, command:String, updateInterval:Long)
case class Jobs(jobs:List[Job])
case class JobResult(itemId:String, data:String, date:Date)
case class JobsStop(host:String)
case object KeepWorking