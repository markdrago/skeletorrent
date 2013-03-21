package main

import org.rogach.scallop._
import akka.actor.ActorSystem
import torrent.Torrent.TorrentInitializationMsg

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val metainfoFileName = trailArg[String]("metainfoFileName", required=true)
  verify()
}

object Main {
  def main(args: Array[String]) {
    val conf = new Conf(args)

    val actorSystem = ActorSystem("sk")
    val skeletorrentSystem = new SkeletorrentSystem(actorSystem)

    val torrentActor = skeletorrentSystem.torrentFactory.getTorrent
    torrentActor ! TorrentInitializationMsg(conf.metainfoFileName())
  }
}
