name := "jetstream"

version := "0.1"

scalaVersion := "2.12.6"
val akkaVersion = "2.5.14"
val akkaHttpVersion = "10.1.4"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-jdk14" % "1.7.25",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "io.argonaut" %% "argonaut" % "6.2.2",
  "org.wvlet.airframe" %% "airframe-log" % "0.54",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.mockito" % "mockito-all" % "2.0.2-beta" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "net.jadler" % "jadler-all" % "1.3.0" % Test)