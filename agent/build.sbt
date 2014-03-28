name := "agent"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-remote" % "2.3.0",
  "com.typesafe.akka" %% "akka-kernel" % "2.3.0"
)

mainClass  := Some("Main.scala")

exportJars := true