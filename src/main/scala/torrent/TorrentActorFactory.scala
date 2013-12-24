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

    val trackerActorProps = Props(classOf[TrackerActor], IO(Http)(system))
    val outboundPeerProps = Props(classOf[OutboundPeer])
    val tcpManager = IO(Tcp)(system)

    system.actorOf(Props(
      classOf[TorrentActor],
      port,
      MetaInfo.apply(metainfoString),
      tcpManager,
      trackerActorProps,
      outboundPeerProps)
    )
  }
}
