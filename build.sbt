name := "cats-effect-learning"

version := "0.1"

scalaVersion := "2.12.2"

libraryDependencies += "org.typelevel" %% "cats-effect" % "1.0.0-RC2" withSources() withJavadoc()

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")


