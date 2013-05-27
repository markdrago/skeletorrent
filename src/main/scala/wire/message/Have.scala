package wire.message

import akka.util.{ByteStringBuilder, ByteString}

case class Have(piece: Int) extends Message {
  def serialize: ByteString = {
    new ByteStringBuilder()
      .putInt(5)
      .putByte(4.toByte)
      .putInt(piece.toInt)
      .result()
  }
}

object Have extends MessageParser {
  def apply(str: ByteString): Have = {
    require(str.length > 5, "ByteString is too short to contain a valid Have message")

    val length = fourBytesToInt(str)
    require(length == 5, s"Have message have a fixed length of 5, but length $length found")
    require(str(4) == 4.toByte, s"Have message must be of message type 4, but ${str(4)} found")

    val piece = fourBytesToInt(str.drop(5))
    new Have(piece)
  }
}