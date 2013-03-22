name := "skeletorrent"

version := "0.1"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Spray Repository" at "http://nightlies.spray.io"

seq(ScctPlugin.instrumentSettings : _*)

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "com.typesafe.akka" %% "akka-actor" % "2.1.1",
    "com.typesafe.akka" %% "akka-testkit" % "2.1.1",
    "commons-codec" % "commons-codec" % "1.7",
    "commons-io" % "commons-io" % "2.4",
    "org.rogach" %% "scallop" % "0.8.0",
    "io.spray" % "spray-client" % "1.1-20130207",
    "io.spray" % "spray-http" % "1.1-20130207",
    "io.spray" % "spray-httpx" % "1.1-20130207",
    "io.spray" % "spray-util" % "1.1-20130207",
    "io.spray" % "spray-can" % "1.1-20130207"
)
