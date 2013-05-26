package wire.message

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import akka.util.{ByteStringBuilder, ByteString}
import java.nio.ByteOrder

class BitfieldTest extends FunSuite with ShouldMatchers {
  implicit val bo = ByteOrder.BIG_ENDIAN

  //bitfield message which indicates the 0th piece is available
  val validBitfieldMessage = new ByteStringBuilder()
    .putInt(2)
    .putByte(5.toByte)
    .putByte(128.toByte)
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

  test("convertByteStringToSet when given empty string returns empty set") {
    Bitfield.convertByteStringToSet(ByteString()) should be (Set.empty)
  }

  test("byteToIntSet works for empty byte") {
    Bitfield.byteToIntSet(0) should be (Set.empty)
  }

  test("byteToIntSet works for non-empty byte") {
    Bitfield.byteToIntSet(128.toByte) should be (Set(0))
  }

  test("byteToIntSet works for complex byte") {
    Bitfield.byteToIntSet(170.toByte) should be (Set(0, 2, 4, 6))
  }

  test("convertByteStringToSet works for simple byte string") {
    val input = new ByteStringBuilder()
      .putBytes(Array.fill(1)(0xAB.toByte))
      .result()
    val result = Bitfield.convertByteStringToSet(input)
    result should be (Set(0, 2, 4, 6, 7))
  }

  test("convertByteStringToSet works for multi-byte string") {
    val input = new ByteStringBuilder()
      .putBytes(Array.fill(1)(0xAB.toByte))
      .putBytes(Array.fill(1)(0x88.toByte))
      .result()
    val result = Bitfield.convertByteStringToSet(input)
    result should be (Set(0, 2, 4, 6, 7, 8, 12))
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

  test("Bitfield parser parses bitfield message < 4 bytes in length correctly") {
    val validButShortBitfieldMessage = new ByteStringBuilder()
      .putInt(2)
      .putByte(5.toByte)
      .putByte(0xC0.toByte)
      .result()
    val result = Bitfield(validButShortBitfieldMessage)
    result should not be ('empty)
    result.contains(0) should be (true)
    result.contains(1) should be (true)
    result.contains(2) should be (false)
  }

  test("intToByteAndOffset works for first bit no offset (0)") {
    val (byte, offset) = Bitfield.intToByteAndOffset(0)
    byte should be (128.toByte)
    offset should be (0)
  }

  test("intToByteAndOffset works for last bit no offset (7)") {
    val (byte, offset) = Bitfield.intToByteAndOffset(7)
    byte should be (1.toByte)
    offset should be (0)
  }

  test("intToByteAndOffset works for first bit offset of 1 (8)") {
    val (byte, offset) = Bitfield.intToByteAndOffset(8)
    byte should be (128.toByte)
    offset should be (1)
  }

  test("intToByteAndOffset works for middle bit larger offset (21)") {
    val (byte, offset) = Bitfield.intToByteAndOffset(21)
    byte should be (4.toByte)
    offset should be (2)
  }

  test("serialize works for empty bitfield") {
    val input = new Bitfield(Set.empty[Int])
    val expected = new ByteStringBuilder()
      .putBytes(Array.fill(3)(0.toByte))
      .putByte(1)
      .putByte(5) //type
      .result()
    input.serialize should be (expected)
  }

  test("serialize works for long bitfield with single piece set") {
    val input = new Bitfield(Set[Int](30))
    val expected = new ByteStringBuilder()
      .putInt(5)
      .putByte(5) //type
      .putBytes(Array.fill(3)(0.toByte)) //move 8*3=24 bits (passed bit #23)
      .putByte((0x80 >> 6).toByte) //set 30th bit (counting from 0 at the left)
      .result()
    input.serialize should be (expected)
  }

  test("serialize works for shorter bitfield with multiple pieces in set") {
    val input = new Bitfield(Set[Int](1, 2, 3, 0))
    val expected = new ByteStringBuilder()
      .putInt(2)
      .putByte(5) //type
      .putByte(0xF0.toByte)  //11110000
      .result()
    input.serialize should be (expected)
  }
}
