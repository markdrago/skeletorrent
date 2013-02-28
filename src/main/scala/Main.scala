package main.scala

import org.rogach.scallop._
import akka.actor.{Props, ActorSystem}
import torrent.Torrent

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val metainfoFileName = trailArg[String]("metainfoFileName", required=true)
  verify
}

object Main {
  def main(args: Array[String]) {
    val conf = new Conf(args)

    val system = ActorSystem("sk")
    val torrentActor = system.actorOf(
      Props(new Torrent(conf.metainfoFileName())),
      name="torrent"
    )
  }
}