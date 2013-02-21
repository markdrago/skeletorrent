name := "skeletorrent"

version := "0.1"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "com.typesafe.akka" %% "akka-actor" % "2.1.0"
)
