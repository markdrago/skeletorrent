package torrent.peer

import akka.actor.{Props, ActorRef, ActorLogging}
import akka.io.Tcp._
import akka.util.ByteString
import java.net.{InetAddress, InetSocketAddress}
import wire.message.Handshake

class OutboundPeer(
  tcpManager: ActorRef,
  val otherPeerId: ByteString,
  host: String,
  port: Int,
  infoHash: ByteString) extends Peer with ActorLogging {

  //initiate connection
  tcpManager ! Connect(new InetSocketAddress(InetAddress.getByName(host), port))

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      log.warning(s"Failed to connect to outbound peer: $host:$port")
      context stop self
    case Connected(remote, local)  =>
      //TODO: report connection to parent for state tracking?
      val connection = sender
      connection ! Register(self)
      initiateHandshake(connection)
      context.become(steadyState(connection))
  }

  private[this] def initiateHandshake(connection: ActorRef) {
    val handshake = new Handshake(infoHash, otherPeerId)
    //TODO: probably want to do some acking
    connection ! Write(handshake.serialize, NoAck)
  }
}

object OutboundPeer {
  type OutboundPeerPropsFactory = (ActorRef, ByteString, String, Int, ByteString) => Props

  def props: OutboundPeerPropsFactory = (
    tcpManager: ActorRef,
    peerId: ByteString,
    host: String,
    port: Int,
    infoHash: ByteString) =>
    Props(classOf[OutboundPeer], tcpManager, peerId, host, port, infoHash)
}
