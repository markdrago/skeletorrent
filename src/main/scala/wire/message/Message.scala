package wire.message

import akka.util.ByteString
import java.nio.ByteOrder

trait Message {
  implicit val bo = ByteOrder.BIG_ENDIAN

  def serialize: ByteString
}

trait MessageParser {
  def apply(str: ByteString): Message

  //parse first 4 bytes of message in to an int as that is the prefixed length
  protected[message] def getMessageLength(str: ByteString): Int = {
    val length = bytesToLong(str.take(4))
    require(length <= Int.MaxValue, "Message claims to be bigger than Int.MaxValue")
    length.toInt
  }

  protected[message] def bytesToLong(str: ByteString): Long = {
    require(str.length == 4, s"bytesToLong requires a 4-byte ByteString, but arg has length of ${str.length}")

    //ANDing with 0xFF ensures that byte is interpreted as an unsigned int
    str.foldLeft[Long](0L)((result: Long, b: Byte) =>
      (result * 256L) + (b & 0xFF)
    )
  }
}
