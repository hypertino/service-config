import sbt.Keys._

name := "service-config"

version := "0.1"

organization := "eu.inn"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.8")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
