name := """play-issue-7936"""
organization := "org.to-om"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.github.sbridges" % "ephemeralfs" % "1.0.1.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.to-om.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "org.to-om.binders._"
