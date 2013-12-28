package wire.message

import akka.util.{ByteStringBuilder, ByteString}
import org.scalatest.{Matchers, FunSuite}

class MessageTest extends FunSuite with Matchers {
  object SimpleParser extends MessageParser {
    def apply(str: ByteString): Message = throw new NotImplementedError()
  }

  test("fourBytesToInt should correctly parse first 4 bytes of message in to integer - simple") {
    val length = SimpleParser.fourBytesToInt(
      new ByteStringBuilder()
        .putBytes(Array.fill(3)(0.toByte))
        .putByte(1.toByte)
        .result()
    )
    length should be (1)
  }

  test("fourBytesToInt should correctly parse first 4 bytes of message in to integer - interesting") {
    val length = SimpleParser.fourBytesToInt(
      new ByteStringBuilder()
        .putBytes(Array.fill(2)(0.toByte))
        .putBytes(Array.fill(2)(1.toByte))
        .result()
    )
    length should be (257)
  }

  test("fourBytesToInt should throw IAE when length appears to be bigger than Int.MaxValue") {
    val caught = intercept[IllegalArgumentException] {
      SimpleParser.fourBytesToInt(
        new ByteStringBuilder()
          .putBytes(Array.fill(4)(170.toByte))
          .result()
      )
    }
    caught.getMessage should include ("Int.MaxValue")
  }

  test("fourBytesToInt should throw IAE when given a short ByteString") {
    val caught = intercept[IllegalArgumentException] {
      SimpleParser.fourBytesToInt(ByteString())
    }
    caught.getMessage should include ("at least a 4-byte ByteString")
  }

  test("fourBytesToInt should correctly parse first 4 bytes of message in to integer - complex") {
    val length = SimpleParser.fourBytesToInt(
      new ByteStringBuilder()
        .putBytes(Array.fill(2)(0.toByte))
        .putBytes(Array.fill(2)(170.toByte))
        .result()
    )
    length should be (0xAAAAL)
  }
}
