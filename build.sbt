name := "cats-experiments"

scalaVersion := "2.12.8"

version := "0.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "1.2.0" withSources() withJavadoc(),
  "org.typelevel" %% "cats-core"   % "1.5.0" withSources() withJavadoc()
)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ypartial-unification")


