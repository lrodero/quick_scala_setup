name := "cats-tagless-experiments"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ypartial-unification")

addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M11" cross CrossVersion.full)

libraryDependencies += 
  "org.typelevel" %% "cats-tagless-macros" % "0.1.0" withSources() withJavadoc()
