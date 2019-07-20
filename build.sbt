name := "scalawithcats"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies +=
  "org.typelevel" %% "cats-core" % "1.6.0" withSources() withJavadoc()

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Ypartial-unification")

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3") // For things like MyValidate[E, *]


