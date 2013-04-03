package torrent

import akka.actor.{ActorRef, Props, Actor}
import peer.{TorrentStateTag, OutboundPeer, OutboundPeerConnection}
import akka.util.{ByteString, Timeout}
import concurrent.ExecutionContext
import java.net.InetSocketAddress
import torrent.Torrent._
import bencoding.messages.{TrackerResponse, MetaInfo}
import spray.io.IOBridge.Bind
import spray.io.IOServer.Bound
import concurrent.duration._
import tracker.TrackerAnnouncer.{TrackerRequest, TrackerAnnounceRequestMsg, TrackerAnnounceResponseMsg}

class Torrent(val port: Int,
              val peerId: String,
              val metainfo: MetaInfo,
              peerAccepter: ActorRef,
              trackerAnnouncer: ActorRef) extends Actor {
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
      peerAccepter ! Bind(new InetSocketAddress(port), 100, new TorrentStateTag(self, metainfo.infoHash, peerId))
  }

  private def awaitingBindingState: Receive = {
    case Bound(_, _) =>
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
  }

  private def handleTrackerResponse(resp: TrackerResponse) {
    resp.peers.foreach((peer) => {
      println(s"discovered peer: id: ${peer.peerId}, ip: ${peer.ip}, port: ${peer.port}")
      connectToPeer(peer.peerId, peer.ip, peer.port)
    })
  }

  private def connectToPeer(otherPeerId: ByteString, host: String, port: Int) {
    val conn = context.actorOf(Props[OutboundPeerConnection])
    val tag = TorrentStateTag(self, metainfo.infoHash, peerId)
    context.system.actorOf(Props(new OutboundPeer(conn, tag, otherPeerId, host, port)))
  }

  private def registerPeer(peer: ActorRef) {
    peers = peer :: peers
    println("peer registered with torrent")
  }
}

object Torrent {
  case class TorrentStartMsg()
  case class RegisterPeerWithTorrent(peer: ActorRef)
}
