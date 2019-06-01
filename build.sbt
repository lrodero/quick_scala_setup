name := "fs2-experiments"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")

// available for Scala 2.11, 2.12
libraryDependencies += "co.fs2" %% "fs2-core" % "1.0.4" withSources() withJavadoc() // For cats 1.5.0 and cats-effect

// optional I/O library
libraryDependencies += "co.fs2" %% "fs2-io" % "1.0.4" withSources() withJavadoc()

