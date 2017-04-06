name := "shapelessguide"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions in Global ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-encoding", "UTF-8",
  "-Xlint",
  "-Xfatal-warnings",
  "-Ywarn-dead-code"
)

libraryDependencies in Global ++= Seq(
  "com.chuusai"   %% "shapeless"     % "2.3.2" withSources() withJavadoc(),
  "org.typelevel" %% "cats"          % "0.7.0" withSources() withJavadoc(),
  "io.circe"      %% "circe-core"    % "0.7.0-M1" withSources() withJavadoc(),
  "io.circe"      %% "circe-generic" % "0.7.0-M1" withSources() withJavadoc(),
  "io.circe"      %% "circe-parser"  % "0.7.0-M1" withSources() withJavadoc(),
  "org.scalactic" %% "scalactic"     % "2.2.6" % Test withSources() withJavadoc(),
  "org.scalatest" %% "scalatest"     % "2.2.6" % Test withSources() withJavadoc()
)

