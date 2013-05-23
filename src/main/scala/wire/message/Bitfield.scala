package wire.message

import akka.util.ByteString
import scala.collection.immutable.BitSet

class Bitfield(pieces: Set[Int]) extends Message {
  def serialize: ByteString = ???
  def contains(x: Int) = pieces contains x
  def isEmpty = pieces.isEmpty
}

object Bitfield extends MessageParser {
  def apply(str: ByteString): Bitfield = {
    require(str.length >= 5, "Bitfield message is too short to possibly contain full message")
    require(str(4).toInt == 5, "Bitfield message must have message type of '5'")

    val messageLength = getMessageLength(str)
    require(messageLength >= 1,
      "Bitfield message claims length less than minimum of 1")
    require(messageLength + 4 <= str.length,
      "Bitfield message claims to be longer than the buffer length")

    //real bitfield will have length of (message length - 1) (for message type)
    val bitfieldLength = messageLength - 1

    new Bitfield(
      BitSet.fromBitMask(convertByteStringToLongSeq(str.drop(5).take(bitfieldLength)).toArray)
    )
  }

  private[message] def convertByteStringToLongSeq(str: ByteString): List[Long] = {
    def recur(str: ByteString): List[Long] = {
      if (str.length == 0) Nil
      else bytesToLong(str.take(4)) :: recur(str.drop(4))
    }

    val validStr =
      if (str.length % 4 != 0)
        ByteString(Array.fill(4 - (str.length % 4))(0.toByte)) ++ str
      else
        str

    recur(validStr)
  }
}