name := """play-java-swan-cloud"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42",
  "org.json"%"org.json"%"chargebee-1.0",
  "javax.mail" % "mail" % "1.4.7",
  "com.twitter" % "hbc-core" % "2.2.0",
  "com.rabbitmq" % "amqp-client" % "4.1.0",
  "org.hibernate" % "hibernate-spatial" % "5.1.0.Final",
  "com.vividsolutions" % "jts" % "1.13",
  "dom4j" % "dom4j" % "1.6.1",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "com.opencsv" % "opencsv" % "4.0",
  "com.googlecode.json-simple" % "json-simple" % "1.1",
  "com.tinkerpop.blueprints" % "blueprints-core" % "2.6.0",
  "org.moeaframework" % "moeaframework" % "2.12",
  "org.apache.poi" % "poi-ooxml" % "3.11"
)


