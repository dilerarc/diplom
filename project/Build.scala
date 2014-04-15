import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object BuildSettings {

  val buildOrganization = "com.ucheck"
  val buildVersion      = "0.1"
  val buildScalaVersion = "2.10.3"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-feature")
  )
}

object Resolvers {
  val ts = "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
}

object Dependencies {
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % "2.2.4"
  val akkaKernel = "com.typesafe.akka" %% "akka-kernel" % "2.2.4"
  val salat = "com.novus" %% "salat" % "1.9.5"
  val joda = "com.github.nscala-time" %% "nscala-time" % "0.8.0"
  val mailer = "com.typesafe" %% "play-plugins-mailer" % "2.1-RC2"
}

object DiplomBuid extends Build {

  import Resolvers._
  import Dependencies._
  import BuildSettings._

  val commonDeps = Seq (
    joda
  )

  val agentDeps = commonDeps ++ Seq (
    akkaRemote % "provided",
    akkaKernel % "provided"
  )

  val baseDeps = commonDeps ++ Seq (
    akkaRemote,
    akkaKernel,
    salat,
    mailer
  )

  lazy val diplom = Project (
    "diplom",
    file ("."),
    settings = buildSettings
  ) aggregate (base, agent, common)

  lazy val common = Project (
    "common",
    file ("common"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= commonDeps
    )
  )

  lazy val agent = Project (
    "agent",
    file ("agent"),
    settings = buildSettings ++ assemblySettings ++ Seq (
      mainClass := Some("Main.scala"),
      jarName in assembly := "agent.jar",
      assemblyOption in assembly ~= { _.copy(includeScala = false) },
      libraryDependencies ++= agentDeps

    )
  ) dependsOn common

  lazy val base = Project (
    "base",
    file ("base"),
    settings = buildSettings  ++ play.Project.playScalaSettings ++ Seq (
      libraryDependencies ++= baseDeps
    )
  ) dependsOn common
}