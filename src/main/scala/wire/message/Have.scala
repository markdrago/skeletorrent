package wire.message

import akka.util.ByteString

case class Have(piece: Long) extends Message {
  def serialize: ByteString = ???
}

object Have extends MessageParser {
  def apply(str: ByteString): Have = {
    require(str.length > 5, "ByteString is too short to contain a valid Have message")

    val length = getMessageLength(str)
    require(length == 5, s"Have message have a fixed length of 5, but length $length found")
    require(str(4) == 4.toByte, s"Have message must be of message type 4, but ${str(4)} found")

    val piece = bytesToLong(str.drop(5))
    new Have(piece)
  }
}