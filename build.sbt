name := "diplom"

version := "0.1"

scalaVersion := "2.10.3"

lazy val agent = project

lazy val base = project.dependsOn(agent)

lazy val diplom = project.in( file(".") ).aggregate(agent, base)