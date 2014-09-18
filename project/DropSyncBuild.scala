import sbt._
import sbt.Keys._

import sbtassembly.Plugin._
import AssemblyKeys._

object DropSyncBuild extends Build {

  lazy val dropsync = Project(
    id = "dropsync",
    base = file("."),
    settings = Project.defaultSettings ++ assemblySettings ++ Seq(
      name := "dropsync",
      organization := "com.github.tanacasino",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.11.2",
      // add other settings here
      libraryDependencies ++= Seq(
        "com.dropbox.core" % "dropbox-core-sdk" % "[1.7,1.8)",
        "com.typesafe" % "config" % "1.2.1",
        "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
      )
    )
  )

}
