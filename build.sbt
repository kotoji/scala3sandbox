val scala3Version = "3.4.0"

ThisBuild / organization := "com.example"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3Version

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3sandbox",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

lazy val fpscala = project
  .in(file("book-fpscala"))
  .settings(
    name := "fpscala",
    Compile / mainClass := Some("fpscala.Main"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.4.5",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test
    )
  )
