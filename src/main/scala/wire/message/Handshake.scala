package wire.message

import akka.util.{ByteStringBuilder, ByteString}

class Handshake(val infohash: ByteString, val peerid: ByteString) extends Message

object Handshake {
  val expectedHeader = "BitTorrent protocol"
  val protoIdLength = 1 + expectedHeader.length  //single byte is proto id len
  val byteFlagsLength = 8
  val infoHashLength = 20
  val peerIdLength = 20

  def apply(str: ByteString): Handshake = {
    verifyHandshakeMessageLength(str)
    verifyProtocolIdentifier(str)

    val infoHashLocation =  protoIdLength + byteFlagsLength
    val peerIdLocation = infoHashLocation + infoHashLength

    new Handshake(str.slice(infoHashLocation, infoHashLocation + infoHashLength),
                  str.slice(peerIdLocation, peerIdLocation + peerIdLength))
  }

  private[this] def verifyHandshakeMessageLength(str: ByteString) {
    require(str.length >= protoIdLength + byteFlagsLength + infoHashLength + peerIdLength,
      "Handshake message is too short to contain all required data")
  }

  private[this] def verifyProtocolIdentifier(str: ByteString) {
    val expected = new ByteStringBuilder()
    .putByte(expectedHeader.length.toByte)
    .putBytes(expectedHeader.getBytes("UTF-8"))
    .result()
    require(str.startsWith(expected),
      "Handshake must start with bittorrent protocol identifier")
  }
}