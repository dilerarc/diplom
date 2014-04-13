package com.ucheck.common

import org.joda.time.DateTime

case class Job(itemId:String, command:String, updateInterval:Long)
case class Jobs(jobs:List[Job])
case class JobResult(itemId:String, data:String, date:DateTime)
case class JobsStop(host:String)