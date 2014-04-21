package com.ucheck.common

import org.joda.time.DateTime

case class Job(itemId:String, command:String, updateInterval:Long)
sealed trait Jobs {
  def jobs:List[Job]
}
case class SNMPJobs(jobs:List[Job]) extends Jobs
case class SimpleJobs(jobs:List[Job]) extends Jobs
case class AgentJobs(jobs:List[Job]) extends Jobs
case class JobResult(itemId:String, data:String, date:DateTime)
case class JobsStop(host:String)
case class JobsStopAll()
case class Email(email:String, subject:String, body:String)