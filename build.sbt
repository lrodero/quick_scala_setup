name := "Experiments"

version := "0.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked")

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.0"
)
