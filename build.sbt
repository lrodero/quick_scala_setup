name := "scalawithcats"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies +=
  "org.typelevel" %% "cats-core" % "1.0.0" withSources() withJavadoc()

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Xfatal-warnings",
  "-Ypartial-unification")


