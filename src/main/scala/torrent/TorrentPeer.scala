package torrent

import akka.actor.{ActorRef, Actor}
import spray.io.IOBridge.Connection

class TorrentPeer(torrent: ActorRef, connection: Connection) extends Actor {
  println("constructed torrent peer")

  override def receive = {
    case _ => println("torrent peer got message")
  }
}
