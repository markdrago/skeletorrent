package wire.message

import akka.util.{ByteStringBuilder, ByteString}
import java.nio.ByteOrder

trait Message {
  implicit val bo = ByteOrder.BIG_ENDIAN

  def serialize: ByteString
}

//messages of length 1 and no payload
trait SimpleMessage extends Message {
  def serializeWithMessageType(messageType: Byte): ByteString = {
    new ByteStringBuilder()
      .putInt(1)
      .putByte(messageType)
      .result()
  }
}

trait MessageParser {
  def apply(str: ByteString): Message

  //parse first 4 bytes of message in to an int
  protected[message] def fourBytesToInt(str: ByteString): Int = {
    require(str.length >= 4, s"fourBytesToInt requires at least a 4-byte ByteString, but arg has length of ${str.length}")

    //ANDing with 0xFF ensures that byte is interpreted as an unsigned int
    val result = str.take(4).foldLeft[Long](0L)((result: Long, b: Byte) =>
      (result * 256L) + (b & 0xFF)
    )

    //BitTorrent spec specifies that 4 bytes is an unsigned int,
    //but a bunch of things in skeletorrent/scala/etc. expect an int,
    //(Seq.apply, max length of ByteString, etc.) so I made a few concessions
    //skeletorrent will not support torrents with > 2.1B pieces for example  :-)
    require(result <= Int.MaxValue, "Parsed 4-byte Long claims to be bigger than Int.MaxValue")
    result.toInt
  }
}

//message validator for messages with length 1 and no payload
trait SimpleMessageValidator extends MessageParser {
  def checkValidForMessageType(str: ByteString, messageType: Byte, typeName: String) {
    require(str.length >= 5, s"ByteString is too short to be a valid $typeName message")

    val messageLength = fourBytesToInt(str)
    require(messageLength == 1, s"$typeName messages require claimed size of 1, but $messageLength found")

    require(str(4) == messageType, s"$typeName message requires type of $messageType, but ${str(4)} found")
  }
}
