package wire.message

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import akka.util.{ByteStringBuilder, ByteString}

class BitfieldTest extends FunSuite with ShouldMatchers {
  //bitfield message which indicates the 0th piece is available
  val validBitfieldMessage = new ByteStringBuilder()
    .putBytes(Array.fill(3)(0.toByte))
    .putByte(5.toByte)
    .putByte(5.toByte)
    .putBytes(Array.fill(3)(0.toByte))
    .putByte(1.toByte)
    .result()

  //bitfield which contains no information about pieces (seen often in wild)
  val validEmptyBitfieldMessage = new ByteStringBuilder()
    .putBytes(Array.fill(3)(0.toByte))
    .putByte(1.toByte)
    .putByte(5.toByte)
    .result()

  test("Bitfield parser throws IAE when given blank bytestring") {
    val caught = intercept[IllegalArgumentException] {
      Bitfield(ByteString())
    }
    caught.getMessage should include ("too short")
  }

  test("Bitfield parser throws IAE when given message with incorrect type") {
    val caught = intercept[IllegalArgumentException] {
      Bitfield(validEmptyBitfieldMessage.updated(4, 4.toByte))
    }
    caught.getMessage should include ("message type")
  }

  test("Bitfield parser throws IAE when message claims a length less than 1") {
    val caught = intercept[IllegalArgumentException] {
      Bitfield(validEmptyBitfieldMessage.updated(3, 0.toByte))
    }
    caught.getMessage should include ("minimum of 1")
  }

  test("Bitfield parser throws IAE when message claims a length greater than the string length") {
    val caught = intercept[IllegalArgumentException] {
      Bitfield(validEmptyBitfieldMessage.updated(3, 255.toByte))
    }
    caught.getMessage should include ("longer than the buffer length")
  }

  test("Bitfield parser parses empty bitfield message correctly") {
    val result = Bitfield(validEmptyBitfieldMessage)
    result should be ('empty)
  }

  test("Bitfield parser parses non-empty bitfield message correctly") {
    val result = Bitfield(validBitfieldMessage)
    result should not be ('empty)
    result.contains(0) should be (true)
  }

  //TODO: problem with padding on left/right, multiple of 4, etc.
  test("Bitfield parser parses bitfield message < 4 bytes in length correctly") {
    val validButShortBitfieldMessage = new ByteStringBuilder()
      .putBytes(Array.fill(3)(0.toByte))
      .putByte(4.toByte)
      .putByte(5.toByte)
      .putByte(0.toByte)
      .putByte(0.toByte)
      .putByte(3.toByte)
      //.putByte(3.toByte)
      .result()
    val result = Bitfield(validButShortBitfieldMessage)
    result should not be ('empty)
    result.contains(0) should be (true)
    result.contains(1) should be (true)
    result.contains(2) should be (false)
  }

  test("convertByteStringToLongSeq when given empty string returns empty array") {
    Bitfield.convertByteStringToLongSeq(ByteString()) should be (Nil)
  }

  test("convertByteStringToLongSeq works for simple byte string (== 4 bytes)") {
    val input = new ByteStringBuilder()
      .putBytes(Array.fill(4)(0xAB.toByte))
      .result()
    val result = Bitfield.convertByteStringToLongSeq(input)
    result should be (List(0xABABABABL))
  }

  test("convertByteStringToLongSeq works for short byte string (< 4 bytes)") {
    val input = new ByteStringBuilder()
      .putBytes(Array.fill(2)(0xAB.toByte))
      .result()
    val result = Bitfield.convertByteStringToLongSeq(input)
    result should be (List(0x0000ABABL))
  }

  test("convertByteStringToLongSeq works for long byte string (> 4 bytes)") {
    val input = new ByteStringBuilder()
      .putBytes(Array.fill(4)(0xAB.toByte))
      .putBytes(Array.fill(4)(0xDE.toByte))
      .result()
    val result = Bitfield.convertByteStringToLongSeq(input)
    result should be (List(0xABABABABL, 0xDEDEDEDEL))
  }
}
