name := "experiments"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")

libraryDependencies += "io.monix" %% "monix" % "2.1.2" withSources() withJavadoc()


