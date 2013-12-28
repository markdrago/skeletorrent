package torrent.peer

import akka.actor._
import akka.event.Logging
import akka.io.Tcp._
import akka.util.ByteString
import utils.Utils
import wire.message.Handshake

class Peer(
  connection: ActorRef,
  peerType: PeerType,
  infoHash: ByteString,
  peerId: ByteString,
  var remotePeerId: Option[ByteString]) extends Actor with ActorLogging {

  override val log = Logging(context.system, this)

  registerAsConnectionHandler()
  if (peerType == PeerTypeOutbound) sendInitialHandshake()

  def receive: Receive = {
    case Received(buffer) => handleReceive(buffer)
    case CommandFailed(w: Write) => log.error("torrent peer got command failed(write)")
    case _: ConnectionClosed => log.error("torrent peer got message to close")
    case _ => log.error("torrent peer got unknown message")
  }

  private[this] def handleReceive(buffer: ByteString) {
    println("torrent peer recieved data")
    println("from: " + remotePeerId.get.decodeString("UTF-8"))
    println("data: " + legibleData(buffer))
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

  def registerAsConnectionHandler() {
    connection ! Register(self)
  }

  def sendInitialHandshake() {
    //TODO: handle lack of an otherPeerId
    val handshake = new Handshake(infoHash, remotePeerId.get)
    //TODO: probably want to do some acking
    connection ! Write(handshake.serialize, NoAck)
  }
}

object Peer {
  type PeerPropsFactory = (ActorRef, PeerType, ByteString, ByteString, Option[ByteString]) => Props

  def props: PeerPropsFactory = (
    connection: ActorRef,
    peerType: PeerType,
    infoHash: ByteString,
    peerId: ByteString,
    remotePeerId: Option[ByteString]) =>
    Props(classOf[Peer], connection, peerType, infoHash, peerId, remotePeerId)
}