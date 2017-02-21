name := "akka-experiments"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.17"
