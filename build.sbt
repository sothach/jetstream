name := "jetstream"

version := "0.1"

scalaVersion := "2.12.6"
val akkaVersion = "2.5.14"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.play" %% "play-json" % "2.6.8",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.mockito" % "mockito-all" % "2.0.2-beta" % Test,
  "net.jadler" % "jadler-all" % "1.3.0" % Test)