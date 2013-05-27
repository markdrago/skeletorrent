package wire.message

import akka.util.{ByteStringBuilder, ByteString}
import scala.collection.immutable.{SetProxy, BitSet}

case class Bitfield(pieces: Set[Int]) extends Message with SetProxy[Int] {
  def self: Set[Int] = pieces

  def serialize: ByteString = {
    val bits = getByteStringOfPieces
    val length = bits.length + 1

    new ByteStringBuilder()
      .putInt(length)
      .putByte(5)
      .result() ++ bits
  }

  private def getByteStringOfPieces: ByteString = {
    if (pieces.isEmpty) ByteString()
    else {
      val (byte, offset) = Bitfield.intToByteAndOffset(pieces.head)
      val nextByteString = (new Bitfield(pieces.tail)).getByteStringOfPieces
      val byteToPlace = (nextByteString.lift(offset).getOrElse(0.toByte) | byte).toByte
      nextByteString.padTo(offset + 1, 0.toByte).updated(offset, byteToPlace)
    }
  }
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

    //bitfield component is one byte shorter than message as message has
    //1 byte for msg type and the rest is the bitfield
    val bitfieldLength = messageLength - 1

    new Bitfield(
      convertByteStringToSet(str.drop(5).take(bitfieldLength))
    )
  }

  //byte string has bits numbered from 0 at the left-most bit and going up towards the right
  //if the bit is set it indicates that the bitfield contains that piece
  //so, if only the 0th piece is available, the first byte will be 10000000
  //if the 0th and the 3rd are available, the first byte will be 10010000, etc.
  private[message] def convertByteStringToSet(str: ByteString, offset: Int = 0): Set[Int] =
    if (str.isEmpty) BitSet()
    else convertByteStringToSet(str.tail, offset + 1) ++ (byteToIntSet(str.head).map(_ + (offset * 8)))

  private[message] def byteToIntSet(b: Byte, shift: Int = 0): Set[Int] =
    (for (shift <- 0 to 7 if ((0x80 >> shift) & b) != 0) yield shift).toSet

  //convert an integer to a byte (with 1 bit set) and an int (offset of bytes from start)
  //0 -> (1000000, 0)
  //9 -> (0100000, 1)
  private[message] def intToByteAndOffset(i: Int): (Byte, Int) = {
    (((0x80 >> i % 8) & 0xFF).toByte, i / 8)
  }
}