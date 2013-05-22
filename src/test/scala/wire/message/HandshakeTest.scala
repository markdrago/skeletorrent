package wire.message

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import akka.util.{ByteStringBuilder, ByteString}

class HandshakeTest extends FunSuite with ShouldMatchers {
  val protoId = "BitTorrent protocol"
  val infohash = Array.fill(20)(0xAB.toByte)
  val peerid = Array.fill(20)(0x12.toByte)
  val validHandshakeMessage = new ByteStringBuilder()
    .putByte(protoId.length.toByte)
    .putBytes(protoId.getBytes("UTF-8"))
    .putBytes(Array.fill(8)(0.toByte))
    .putBytes(infohash)
    .putBytes(peerid)
    .result()

  test("Handshake throws IllegalArgument when given blank ByteString") {
    val caught = intercept[IllegalArgumentException] {
      Handshake(ByteString())
    }
    caught.getMessage should include ("too short")
  }

  test("Handshake throws IllegalArgument when given ByteString that is too short") {
    val caught = intercept[IllegalArgumentException] {
      Handshake(ByteString("Identifier goes here"))
    }
    caught.getMessage should include ("too short")
  }

  test("Handshake throws IllegalArgument if protocol identifier does not match") {
    val invalidProtoId = "MyFavorite Protocol"
    val messageWithInvalidProtoId =
      new ByteStringBuilder()
      .putByte(invalidProtoId.length.toByte)
      .putBytes(invalidProtoId.getBytes("UTF-8"))
      .result() ++
      validHandshakeMessage.drop(1 + protoId.length)
    val caught = intercept[IllegalArgumentException] {
      Handshake(messageWithInvalidProtoId)
    }
    caught.getMessage should include ("protocol identifier")
  }

  test("Handshake parses infohash") {
    val result = Handshake(validHandshakeMessage)
    result.infohash should be (ByteString(infohash))
  }

  test("Handshake parses peerid") {
    val result = Handshake(validHandshakeMessage)
    result.peerid should be (ByteString(peerid))
  }

  test("Handshake can serialize correctly") {
    val handshake = new Handshake(ByteString(infohash), ByteString(peerid))
    handshake.serialize should be (validHandshakeMessage)
  }
}
