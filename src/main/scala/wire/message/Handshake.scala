package wire.message

import akka.util.{ByteStringBuilder, ByteString}

case class Handshake(infohash: ByteString, peerid: ByteString) extends Message {
  def serialize: ByteString = {
    new ByteStringBuilder()
      .putByte(Handshake.protocolId.length.toByte)
      .putBytes(Handshake.protocolId.getBytes("UTF-8"))
      .putBytes(Array.fill(8)(0.toByte))
      .result() ++
      infohash ++
      peerid
  }
}

object Handshake extends MessageParser {
  val protocolId = "BitTorrent protocol"
  val protocolIdLength = 1 + protocolId.length  //single byte is proto id len
  val byteFlagsLength = 8
  val infoHashLength = 20
  val peerIdLength = 20

  def apply(str: ByteString): Handshake = {
    verifyHandshakeMessageLength(str)
    verifyProtocolIdentifier(str)

    val infoHashLocation =  protocolIdLength + byteFlagsLength
    val peerIdLocation = infoHashLocation + infoHashLength

    new Handshake(str.slice(infoHashLocation, infoHashLocation + infoHashLength),
                  str.slice(peerIdLocation, peerIdLocation + peerIdLength))
  }

  private[this] def verifyHandshakeMessageLength(str: ByteString) {
    require(str.length >= protocolIdLength + byteFlagsLength + infoHashLength + peerIdLength,
      "Handshake message is too short to contain all required data")
  }

  private[this] def verifyProtocolIdentifier(str: ByteString) {
    val expected = new ByteStringBuilder()
    .putByte(protocolId.length.toByte)
    .putBytes(protocolId.getBytes("UTF-8"))
    .result()
    require(str.startsWith(expected),
      "Handshake must start with bittorrent protocol identifier")
  }
}