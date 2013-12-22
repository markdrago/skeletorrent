package main

import org.rogach.scallop._
import akka.actor.ActorSystem
import torrent.Torrent.TorrentStartMsg
import utils.Utils

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val metainfoFileName = trailArg[String]("metainfoFileName", required=true)
  //verify()
}

object Main {
  def main(args: Array[String]) {
    val conf = new Conf(args)

    val actorSystem = ActorSystem("sk")
    val skeletorrentSystem = new SkeletorrentSystem(conf, actorSystem)

    val torrentActor = skeletorrentSystem.torrentFactory.create(
      actorSystem,
      6881,
      Utils.readFile(conf.metainfoFileName.get.get)
    )
    torrentActor ! TorrentStartMsg()
  }
}
