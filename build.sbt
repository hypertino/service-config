name := "service-config"

version := "0.2.6"

organization := "com.hypertino"

crossScalaVersions := Seq("2.12.4", "2.11.12")

scalaVersion := crossScalaVersions.value.head

libraryDependencies ++= Seq(
  "org.scaldi" %% "scaldi" % "0.5.8",
  "com.typesafe" % "config" % "1.2.1",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
)

resolvers ++= Seq(
	Resolver.sonatypeRepo("public")
)
