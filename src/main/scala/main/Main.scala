package main

import akka.actor.ActorSystem
import torrent.TorrentActor.TorrentStartMsg
import torrent.TorrentActorFactory
import utils.Utils

object Main {
  def main(args: Array[String]) {
    val conf = new ArgParser(args)

    val actorSystem = ActorSystem("sk")

    val torrentActor = TorrentActorFactory.create(
      actorSystem,
      6881,
      Utils.readFile(conf.metainfoFileName.get.get)
    )

    torrentActor ! TorrentStartMsg()
  }
}
