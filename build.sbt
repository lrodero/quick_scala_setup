name := "experiments"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")

libraryDependencies += "com.typesafe" % "config" % "1.3.1" withSources() withJavadoc()

