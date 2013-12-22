name := "skeletorrent"

version := "0.1"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

//resolvers += "Spray Repository" at "http://nightlies.spray.io"

//seq(ScctPlugin.instrumentSettings : _*)

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.0" % "test",
    "org.mockito" % "mockito-core" % "1.9.5" % "test",
    "com.typesafe.akka" %% "akka-actor" % "2.2.3",
    "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
    "commons-codec" % "commons-codec" % "1.8",
    "commons-io" % "commons-io" % "2.4",
    "org.rogach" %% "scallop" % "0.9.4",
    "io.spray" % "spray-client" % "1.2.0",
    "io.spray" % "spray-http" % "1.2.0",
    "io.spray" % "spray-httpx" % "1.2.0",
    "io.spray" % "spray-util" % "1.2.0",
    "io.spray" % "spray-can" % "1.2.0"
)
