name := "freemonads"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.6.0" withSources() withJavadoc(),
  "org.typelevel" %% "cats-free" % "1.6.0" withSources() withJavadoc()
)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Ypartial-unification"
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

