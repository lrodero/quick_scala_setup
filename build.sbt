name := "Experiments"

version := "0.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.0",
  "org.scalaz" %% "scalaz-effect" % "7.2.0",
  "org.scalaz" %% "scalaz-typelevel" % "7.2.0",
  "org.scalaz" %% "scalaz-scalacheck-binding" % "7.2.0"
)

initialCommands in console := """
  | import scalaz._
  | import Scalaz._
""".stripMargin
  


