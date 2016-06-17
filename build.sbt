name := """play-java-swan-cloud"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42",
  "org.json"%"org.json"%"chargebee-1.0",
  "javax.mail" % "mail" % "1.4.7"
)


