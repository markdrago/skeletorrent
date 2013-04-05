package torrent

import akka.util.ByteString
import peer.PeerAccepterComponent
import scala.Predef.String
import util.Random
import bencoding.messages.MetaInfo
import tracker.TrackerAnnouncerComponent
import akka.actor.{ActorRef, Props, ActorSystem}
import torrent.peer.OutboundPeerFactoryComponent

trait TorrentFactoryComponent {
  this: PeerAccepterComponent
  with TrackerAnnouncerComponent
  with OutboundPeerFactoryComponent =>
  val torrentFactory: TorrentFactory

  //TODO: possible to inject actor system via cake pattern?

  class TorrentFactory {
    def create(system: ActorSystem, port : Int, metainfoString: ByteString): ActorRef = {
      system.actorOf(Props(
        new Torrent(
          port,
          generatePeerId,
          MetaInfo.apply(metainfoString),
          peerAccepter,
          trackerAnnouncer,
          outboundPeerFactory
        )
      ))
    }

    def generatePeerId: String = {
      val prefix = "-SK0001-"
      prefix + new String((new Random()).alphanumeric.take(20 - prefix.length).toArray)
    }
  }
}
