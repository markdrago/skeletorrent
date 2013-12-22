package torrent.peer

import akka.util.ByteString
import akka.actor.{ActorContext, Props}

trait OutboundPeerFactoryComponent {
  val outboundPeerFactory: OutboundPeerFactory
}

class OutboundPeerFactory {
  def create(context: ActorContext,
             tag: TorrentStateTag,
             peerId: ByteString,
             host: String,
             port: Int) {
    //val conn = context.actorOf(Props[OutboundPeerConnection])
    context.actorOf(Props(new OutboundPeer(tag, peerId, host, port)))
  }
}
