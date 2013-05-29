package wire.message

import akka.util.{ByteStringBuilder, ByteString}

case class KeepAlive() extends Message {
  def serialize: ByteString =
    new ByteStringBuilder()
      .putInt(0)
      .result()
}

object KeepAlive extends MessageParser {
  def apply(str: ByteString): KeepAlive = {
    require(str.length >= 4, "KeepAlive message is too short to possibly contain full message")

    val messageLength = fourBytesToInt(str)
    require(messageLength == 0, s"KeepAlive message must have length of 0, but $messageLength found")

    KeepAlive()
  }
}