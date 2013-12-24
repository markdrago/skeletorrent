package torrent.peer

import akka.actor._
import akka.util.ByteString
import utils.Utils
import akka.event.Logging
import wire.message.Handshake
import akka.io.Tcp._
import akka.io.Tcp
import akka.io.IO
import java.net.{InetAddress, InetSocketAddress}
import akka.io.Tcp.Connected
import akka.io.Tcp.Connect
import torrent.TorrentActor.RegisterPeerWithTorrent
import akka.io.Tcp.Received

sealed trait Peer extends Actor with ActorLogging {
  def connection: ActorRef
  def otherPeerId: ByteString

  override val log = Logging(context.system, this)

  override def receive = {
    case Received(buffer) => handleReceive(buffer)
    case PeerClosed => println("torrent peer got message to close")
    case _ => println("torrent peer got unknown message")
  }

  private[this] def handleReceive(buffer: ByteString) {
    println("torrent peer recieved data")
    println("from: " + otherPeerId.decodeString("UTF-8"))
    println("data: " + legibleData(buffer))
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

/*
class InboundPeer(val _conn: Connection) extends Peer {
  //TODO: inject a factory which creates these connection actors and then unit test that creation
  val connection = context.system.actorOf(Props(new DefaultIOConnectionActor(_conn, EmptyPipelineStage)))

  //TODO: parse handshake from inbound, get a hold of their peer id, verify infohash, etc.
  val otherPeerId = ByteString("originally inbound")
}
*/

object OutboundPeer {
  case class OutboundPeerInit(tag: TorrentStateTag, otherPeerId: ByteString, host: String, port: Int)
}

class OutboundPeer() extends Peer with ActorLogging {
  import OutboundPeer._

  //TODO: horrible
  var connection: ActorRef = null
  var tag: TorrentStateTag = null
  var otherPeerId: ByteString = null
  var host: String = null
  var port: Int = 0

  override def receive: Receive = {
    //connection ! Connect(host, port, tag)
    case OutboundPeerInit(tagMsg, otherPeerIdMsg, hostMsg, portMsg) => {
      tag = tagMsg
      otherPeerId = otherPeerIdMsg
      host = hostMsg
      port = portMsg
      IO(Tcp)(context.system) ! Connect(new InetSocketAddress(InetAddress.getByName(host), port))
      context.become(waitForConnection)
    }
  }

  def waitForConnection: Receive = {
    case Connected(remote, local) => {
      connection = sender
      context.become(super.receive)
      connectionComplete()
    }
    case Status.Failure(e) => {
      //TODO: probably not what akka io does anymore
      //TODO: kill this actor when unable to connect (actually complicated and interesting)
      log.warning(s"Failed to connect to outbound peer: $host:$port")
    }
  }

  private[peer] def connectionComplete() {
    registerPeerWithTorrent(tag)
    initiateHandshake(tag)
  }

  private[this] def initiateHandshake(tag: TorrentStateTag) {
    val handshake = new Handshake(tag.infoHash, ByteString(tag.peerId))
    //TODO: probably want to do some acking
    connection ! Write(handshake.serialize, NoAck)
  }
}
