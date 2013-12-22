package torrent

import akka.actor.{ActorLogging, ActorRef, Actor}
import torrent.peer.{OutboundPeerFactory, TorrentStateTag}
import akka.util.{ByteString, Timeout}
import concurrent.ExecutionContext
import java.net.InetSocketAddress
import torrent.Torrent._
import bencoding.messages.{TrackerResponse, MetaInfo}
import akka.io.Tcp.{Connected, Bind, Bound}
import concurrent.duration._
import tracker.TrackerAnnouncer.{TrackerRequest, TrackerAnnounceRequestMsg, TrackerAnnounceResponseMsg}
import akka.io.{IO, Tcp}

class Torrent(val port: Int,
              val peerId: String,
              val metainfo: MetaInfo,
              trackerAnnouncer: ActorRef,
              outboundPeerFactory: OutboundPeerFactory)
      extends Actor with ActorLogging {
  implicit val ec = ExecutionContext.global
  implicit val timeout: Timeout = 5 seconds span

  var peers: List[ActorRef] = Nil
  var uploaded = 0
  var downloaded = 0
  var left = 0

  def receive = uninitializedState

  private def uninitializedState: Receive = {
    case TorrentStartMsg() =>
      context.become(awaitingBindingState)
      //peerAccepter ! Bind(new InetSocketAddress(port), 100, new TorrentStateTag(self, metainfo.infoHash, peerId))
      IO(Tcp)(context.system) ! Bind(self, new InetSocketAddress(port))
  }

  private def awaitingBindingState: Receive = {
    case Bound(_) =>
      context.become(steadyState)
      trackerAnnouncer ! TrackerAnnounceRequestMsg(TrackerRequest(
        metainfo.trackerUrl,
        metainfo.infoHash,
        peerId,
        port,
        uploaded,
        downloaded,
        left
      ))
  }

  private def steadyState: Receive = {
    case TrackerAnnounceResponseMsg(resp) => handleTrackerResponse(resp)
    case RegisterPeerWithTorrent(peer) => registerPeer(peer)
    case c @ Connected(remote, local) => {
      //TODO: uniquify actor name
      //TODO: for that matter, support inbound at all
      /*
      val handler = context.system.actorOf(Props[InboundPeer], "peer:inbound")
      val connection = sender
      connection ! Register(handler)
      */
    }
  }

  private def handleTrackerResponse(resp: TrackerResponse) {
    resp.peers.foreach((peer) => {
      log.info(s"discovered peer: id: ${peer.peerId}, ip: ${peer.ip}, port: ${peer.port}")
      connectToPeer(peer.peerId, peer.ip, peer.port)
    })
  }

  private def connectToPeer(otherPeerId: ByteString, host: String, port: Int) {
    val tag = TorrentStateTag(self, metainfo.infoHash, peerId)
    outboundPeerFactory.create(context, tag, otherPeerId, host, port)
  }

  private def registerPeer(peer: ActorRef) {
    peers = peer :: peers
    log.info("peer registered with torrent")
  }
}

object Torrent {
  case class TorrentStartMsg()
  case class RegisterPeerWithTorrent(peer: ActorRef)
}
