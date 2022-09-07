name := "traffic-data-excercise"

version := "0.1"

scalaVersion := "2.13.8"

val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.13",
  "org.scalatest" %% "scalatest" % "3.2.13" % "test",
  "com.monovore" %% "decline" % "2.2.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)