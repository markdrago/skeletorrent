package torrent

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.io.{Tcp, IO}
import akka.util.ByteString
import bencoding.messages.MetaInfo
import spray.can.Http
import torrent.peer.OutboundPeer
import tracker.TrackerActor

object TorrentActorFactory {
  def create(
    system: ActorSystem,
    port: Int,
    metainfoString: ByteString): ActorRef = {

    val httpManager = IO(Http)(system)
    val tcpManager = IO(Tcp)(system)

    system.actorOf(Props(
      classOf[TorrentActor],
      port,
      MetaInfo.apply(metainfoString),
      tcpManager,
      httpManager,
      TrackerActor.props _,
      OutboundPeer.props _)
    )
  }
}
