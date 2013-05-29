package torrent.peer

import akka.actor._
import spray.io.{DefaultIOConnectionActor, EmptyPipelineStage, IOClientConnection}
import spray.io.IOBridge.{Connect, Connection}
import akka.util.ByteString
import java.nio.ByteBuffer
import utils.Utils
import spray.io.IOClientConnection.Connected
import spray.io.IOBridge.Received
import spray.io.IOBridge.Closed
import torrent.Torrent.RegisterPeerWithTorrent
import akka.event.Logging
import wire.message.Handshake

sealed trait Peer extends Actor with ActorLogging {
  def connection: ActorRef
  def otherPeerId: ByteString

  override val log = Logging(context.system, this)

  override def receive = {
    case Received(conn, buffer) => handleReceive(conn, buffer)
    case Closed(_, _) => println("torrent peer got message to close")
    case _ => println("torrent peer got unknown message")
  }

  private[this] def handleReceive(conn: Connection, buffer: ByteBuffer) {
    println("torrent peer recieved data")
    println("from: " + otherPeerId.decodeString("UTF-8"))
    println("data: " + legibleData(ByteString(buffer)))
  }

  protected[peer] def registerPeerWithTorrent(tag: TorrentStateTag) {
    tag.torrent ! RegisterPeerWithTorrent(self)
  }

  protected def legibleData(bs: ByteString): String = {
    val hex = Utils.bsToHex(bs)
    val groups = hex.drop(15).grouped(12)
    new StringBuilder()
      .append("length: " + hex.take(12) + "\n")
      .append("type: " + hex.drop(12).take(3) + "\n")
      .append(("" /: groups) { _ + _ + "\n" })
      .result()
  }
}

class InboundPeer(val _conn: Connection) extends Peer {
  //TODO: inject a factory which creates these connection actors and then unit test that creation
  val connection = context.system.actorOf(Props(new DefaultIOConnectionActor(_conn, EmptyPipelineStage)))

  //TODO: parse handshake from inbound, get a hold of their peer id, verify infohash, etc.
  val otherPeerId = ByteString("originally inbound")
}

class OutboundPeerConnection extends IOClientConnection

class OutboundPeer(
    val connection: ActorRef,
    tag: TorrentStateTag,
    val otherPeerId: ByteString,
    host: String,
    port: Int)
      extends Peer with ActorLogging {

  override def preStart() {
    connection ! Connect(host, port, tag)
  }

  override def receive = {
    case Connected(connectionHandle) => {
      context.become(super.receive)
      connectionComplete(connectionHandle)
    }
    case Status.Failure(e) => {
      //TODO: kill this actor when unable to connect (actually complicated and interesting)
      log.warning(s"Failed to connect to outbound peer: $host:$port")
    }
  }

  private[peer] def connectionComplete(connectionHandle: Connection) {
    val tag = connectionHandle.tag match {
      case t:TorrentStateTag => t
      case _ => {
        log.warning("Unexpected tag type found in new outbound peer connection.")
        throw new IllegalArgumentException("Unexpected tag type found in new outbound peer connection.")
      }
    }
    registerPeerWithTorrent(tag)
    initiateHandshake(tag)
  }

  private[this] def initiateHandshake(tag: TorrentStateTag) {
    val handshake = new Handshake(tag.infoHash, ByteString(tag.peerId))
    connection ! spray.io.IOConnection.Send(handshake.serialize.asByteBuffer)
  }
}
