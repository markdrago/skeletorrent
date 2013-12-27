package torrent.peer

import akka.actor._
import akka.event.Logging
import akka.io.Tcp._
import akka.util.ByteString
import torrent.TorrentActor.RegisterPeerWithTorrent
import utils.Utils

trait Peer extends Actor with ActorLogging {
  def otherPeerId: ByteString

  override val log = Logging(context.system, this)

  def steadyState(connection: ActorRef): Receive = {
    case Received(buffer)        => handleReceive(buffer)
    case CommandFailed(w: Write) => log.error("torrent peer got command failed(write)")
    case _: ConnectionClosed     => log.error("torrent peer got message to close")
    case _                       => log.error("torrent peer got unknown message")
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
      .append(("" /: groups) {
      _ + _ + "\n"
    })
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
