package torrent

import akka.actor.{ActorRef, ActorSystem, Props}

trait TorrentFactoryComponent {
  val torrentFactory: TorrentFactory
  class TorrentFactory(actorSystem: ActorSystem) {
    //TODO figure out port to list on in here?
    def getTorrent: ActorRef = {
      actorSystem.actorOf(Props(new Torrent(6881)))
    }
  }
}