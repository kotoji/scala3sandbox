val scala3Version = "3.4.0"

ThisBuild / organization := "com.example"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3Version

val AIRFRAME_VERSION = "24.9.2"
val FS2_VERSION = "3.11.0"
val http4sVersion = "0.23.29"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3sandbox",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.4.5",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.wvlet.airframe" %% "airframe" % AIRFRAME_VERSION, // Dependency injection
      "org.wvlet.airframe" %% "airframe-codec" % AIRFRAME_VERSION, // MessagePack-based schema-on-read codec
      "org.wvlet.airframe" %% "airframe-config" % AIRFRAME_VERSION, // YAML-based configuration
      "org.wvlet.airframe" %% "airframe-control" % AIRFRAME_VERSION, // Library for retryable execution
      "org.wvlet.airframe" %% "airframe-http" % AIRFRAME_VERSION, // HTTP RPC/REST API and clients
      "org.wvlet.airframe" %% "airframe-http-netty" % AIRFRAME_VERSION, // HTTP server (Netty backend)
      "org.wvlet.airframe" %% "airframe-http-grpc" % AIRFRAME_VERSION, // HTTP/2 server (gRPC backend)
      "org.wvlet.airframe" %% "airframe-http-recorder" % AIRFRAME_VERSION, // HTTP recorder and replayer
      "org.wvlet.airframe" %% "airframe-jmx" % AIRFRAME_VERSION, // JMX monitoring
      "org.wvlet.airframe" %% "airframe-jdbc" % AIRFRAME_VERSION, // JDBC connection pool
      "org.wvlet.airframe" %% "airframe-json" % AIRFRAME_VERSION, // Pure Scala JSON parser
      "org.wvlet.airframe" %% "airframe-launcher" % AIRFRAME_VERSION, // Command-line program launcher
      "org.wvlet.airframe" %% "airframe-log" % AIRFRAME_VERSION, // Logging
      "org.wvlet.airframe" %% "airframe-metrics" % AIRFRAME_VERSION, // Metrics units
      "org.wvlet.airframe" %% "airframe-msgpack" % AIRFRAME_VERSION, // Pure-Scala MessagePack
      "org.wvlet.airframe" %% "airframe-rx" % AIRFRAME_VERSION, // ReactiveX interface
      "org.wvlet.airframe" %% "airframe-rx-html" % AIRFRAME_VERSION, // Reactive DOM
      "org.wvlet.airframe" %% "airframe-surface" % AIRFRAME_VERSION, // Object surface inspector
      "org.wvlet.airframe" %% "airframe-ulid" % AIRFRAME_VERSION, // ULID generator
      "co.fs2" %% "fs2-core" % FS2_VERSION,
      "co.fs2" %% "fs2-io" % FS2_VERSION,
      "co.fs2" %% "fs2-reactive-streams" % FS2_VERSION,
      "co.fs2" %% "fs2-scodec" % FS2_VERSION,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion
    ),
    Compile / run / fork := true
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
