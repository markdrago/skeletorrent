package main

import org.rogach.scallop._
import akka.actor.{Props, ActorSystem}
import torrent.Torrent.TorrentInitializationMsg
import torrent.Torrent

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val metainfoFileName = trailArg[String]("metainfoFileName", required=true)
  verify()
}

object Main {
  def main(args: Array[String]) {
    val conf = new Conf(args)

    val actorSystem = ActorSystem("sk")
    new SkeletorrentSystem(actorSystem)

    val torrentActor = actorSystem.actorOf(Props(new Torrent(6881)))
    torrentActor ! TorrentInitializationMsg(conf.metainfoFileName())
  }
}
