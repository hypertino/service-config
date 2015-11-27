import sbt.Keys._

name := "service-config"

version := "0.1"

organization := "eu.inn"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.7")

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"
