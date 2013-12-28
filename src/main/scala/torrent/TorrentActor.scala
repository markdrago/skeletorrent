package torrent

import akka.actor.{ActorLogging, ActorRef, Actor}
import akka.io.Tcp.{Connect, Bind, Bound, Connected}
import akka.util.{ByteString, Timeout}
import bencoding.messages.{AvailablePeerDetails, MetaInfo}
import concurrent.ExecutionContext
import concurrent.duration._
import java.net.{InetAddress, InetSocketAddress}
import scala.util.Random
import torrent.TorrentActor.AvailablePeerSet
import torrent.peer.{PeerTypeInbound, PeerTypeOutbound, Peer}
import tracker.TrackerActor
import tracker.TrackerActor._

class TorrentActor(
  val port: Int,
  val metainfo: MetaInfo,
  tcpManager: ActorRef,
  httpManager: ActorRef,
  trackerActorPropsFactory: TrackerActor.TrackerActorPropsFactory,
  peerPropsFactory: Peer.PeerPropsFactory)
  extends Actor with ActorLogging {

  implicit val ec = ExecutionContext.global
  implicit val timeout: Timeout = 5 seconds span

  var pendingConnections = Map.empty[InetSocketAddress, ByteString]
  var peers = Set.empty[ActorRef]

  //TODO: move to state object
  var uploaded = 0
  var downloaded = 0
  var left = 0

  val peerId = TorrentActor.generatePeerId

  //bind to listening port
  tcpManager ! Bind(self, new InetSocketAddress(port))

  //start up tracker actor
  val trackerProps = trackerActorPropsFactory(httpManager, metainfo.trackerUrl, metainfo.infoHash, peerId, port)
  val trackerActor = context.actorOf(trackerProps)

  def receive = awaitingBindingState

  private def awaitingBindingState: Receive = {
    case Bound(_) =>
      context.become(steadyState)
      trackerActor ! TrackerStart
  }

  private def steadyState: Receive = {
    case AvailablePeerSet(availablePeers) => handleTrackerResponse(availablePeers)
    case c@Connected(remote, _)           => handleSuccessfulConnection(remote)
  }

  private def handleTrackerResponse(availablePeers: Set[AvailablePeerDetails]) {
    availablePeers.foreach((peer) => {
      log.info(s"discovered peer: id: ${peer.peerId}, ip: ${peer.host}, port: ${peer.port}")
      requestConnectionToPeer(peer.peerId, peer.host, peer.port)
    })
  }

  private def requestConnectionToPeer(remotePeerId: ByteString, host: String, port: Int) {
    log.info(s"Requesting connection to peer: $remotePeerId @ $host:$port")
    val remoteAddress = new InetSocketAddress(InetAddress.getByName(host), port)
    tcpManager ! Connect(remoteAddress)
    pendingConnections += (remoteAddress -> remotePeerId)
  }

  private def handleSuccessfulConnection(remote: InetSocketAddress) {
    log.info(s"Successful connection with ${remote.getAddress}:${remote.getPort}")
    val connection = sender

    //if remote address was in pending connections, then this is an outbound peer
    val (peerType, remotePeerId) = pendingConnections.get(remote) match {
      case Some(otherPeerId) =>
        pendingConnections -= remote
        (PeerTypeOutbound, Some(otherPeerId))
      case None              => (PeerTypeInbound, None)
    }

    val peerProps = peerPropsFactory(connection, peerType, metainfo.infoHash, ByteString(peerId), remotePeerId)
    peers += context.actorOf(peerProps)
  }
}

object TorrentActor {
  case class AvailablePeerSet(peers: Set[AvailablePeerDetails])

  def generatePeerId = {
    val prefix = "-SK0001-"
    prefix + new String((new Random).alphanumeric.take(20 - prefix.length).toArray)
  }

}
