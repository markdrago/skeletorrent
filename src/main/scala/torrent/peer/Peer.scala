package torrent.peer

import akka.actor.{Props, Status, ActorRef, Actor}
import spray.io.{DefaultIOConnectionActor, EmptyPipelineStage, IOClientConnection}
import spray.io.IOBridge.{Received, Connect, Connection, Closed}
import spray.io.IOClientConnection.Connected
import akka.util.{ByteString, ByteStringBuilder}
import torrent.Torrent.RegisterPeerWithTorrent
import java.nio.ByteBuffer
import utils.Utils

sealed trait Peer extends Actor {
  def connection: ActorRef
  def otherPeerId: ByteString

  override def receive = {
    case Received(conn, buffer) => handleReceive(conn, buffer)
    case Closed(_, _) => println("torrent peer got message to close")
    case _ => println("torrent peer got unknown message")
  }

  private def handleReceive(conn: Connection, buffer: ByteBuffer) {
    println("torrent peer recieved data")
    println("from: " + otherPeerId.decodeString("UTF-8"))
    println("data: " + legibleData(ByteString(buffer)))
  }

  protected def registerPeerWithTorrent(tag: TorrentStateTag) {
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
    extends Peer {

  connection ! Connect(host, port, tag)

  override def receive = {
    case Connected(conn) => {
      context.become(super.receive)
      connectionComplete(conn)
    }
    case Status.Failure(e) => println("failed to connect to peer")
  }

  private def connectionComplete(connection: Connection) {
    val tag = connection.tag match {
      case t:TorrentStateTag => t
      case _ => throw new IllegalArgumentException("unexpected tag type")
    }
    registerPeerWithTorrent(tag)
    initiateHandshake(tag)
  }

  private def initiateHandshake(tag: TorrentStateTag) {
    connection ! spray.io.IOConnection.Send(handshake(tag).asByteBuffer)
  }

  private def handshake(tag: TorrentStateTag): ByteString = {
    val title = "BitTorrent protocol"
    new ByteStringBuilder()
      .putByte(title.length.toByte)
      .putBytes(title.getBytes("UTF-8"))
      .putBytes(Array.fill(8)(0.toByte))
      .putBytes(tag.infoHash.toArray)
      .putBytes(tag.peerId.getBytes("UTF-8"))
      .result()
  }
}
