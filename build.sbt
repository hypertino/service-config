name := "service-config"

version := "0.2-SNAPSHOT"

organization := "com.hypertino"

scalaVersion := "2.12.1"

crossScalaVersions := Seq("2.12.1", "2.11.8")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

resolvers ++= Seq(
	Resolver.sonatypeRepo("public")
)
