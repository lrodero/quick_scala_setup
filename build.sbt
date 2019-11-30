name := "cats_effect-experiments"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0" withSources() withJavadoc()



