name := "miner"

version := "0.1"

scalaVersion := "2.12.2"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")


libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.0.0" withSources() withJavadoc(),
  "org.rogach" %% "scallop" % "2.1.2" withSources() withJavadoc())

