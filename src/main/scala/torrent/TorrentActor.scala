package torrent

import akka.actor.{Props, ActorLogging, ActorRef, Actor}
import akka.io.Tcp.{Connected, Bind, Bound}
import akka.util.{ByteString, Timeout}
import bencoding.messages.{TrackerResponse, MetaInfo}
import concurrent.ExecutionContext
import concurrent.duration._
import java.net.InetSocketAddress
import scala.util.Random
import torrent.TorrentActor._
import torrent.peer.OutboundPeer.OutboundPeerInit
import torrent.peer.TorrentStateTag
import tracker.TrackerActor.{TrackerRequest, TrackerAnnounceRequestMsg, TrackerAnnounceResponseMsg}

class TorrentActor(
  val port: Int,
  val metainfo: MetaInfo,
  tcpManager: ActorRef,
  trackerActorProps: Props,
  outboundPeerProps: Props)
  extends Actor with ActorLogging {

  implicit val ec = ExecutionContext.global
  implicit val timeout: Timeout = 5 seconds span

  var peers: List[ActorRef] = Nil
  var uploaded = 0
  var downloaded = 0
  var left = 0

  val trackerActor = context.actorOf(trackerActorProps)

  val peerId = generatePeerId

  def generatePeerId: String = {
    val prefix = "-SK0001-"
    prefix + new String((new Random).alphanumeric.take(20 - prefix.length).toArray)
  }

  def receive = uninitializedState

  private def uninitializedState: Receive = {
    case TorrentStartMsg() =>
      context.become(awaitingBindingState)
      //peerAccepter ! Bind(new InetSocketAddress(port), 100, new TorrentStateTag(self, metainfo.infoHash, peerId))
      tcpManager ! Bind(self, new InetSocketAddress(port))
  }

  private def awaitingBindingState: Receive = {
    case Bound(_) =>
      context.become(steadyState)
      trackerActor ! TrackerAnnounceRequestMsg(TrackerRequest(
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
    case RegisterPeerWithTorrent(peer)    => registerPeer(peer)
    case c@Connected(remote, local)       => {
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

    val outboundPeer = context.actorOf(outboundPeerProps)
    outboundPeer ! OutboundPeerInit(tag, otherPeerId, host, port)
    //outboundPeerFactory.create(context, tag, otherPeerId, host, port)
  }

  private def registerPeer(peer: ActorRef) {
    peers = peer :: peers
    log.info("peer registered with torrent")
  }
}

object TorrentActor {

  case class TorrentStartMsg()

  case class RegisterPeerWithTorrent(peer: ActorRef)

}
