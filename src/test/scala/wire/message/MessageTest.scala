package wire.message

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import akka.util.{ByteStringBuilder, ByteString}

class MessageTest extends FunSuite with ShouldMatchers {
  object SimpleParser extends MessageParser {
    def apply(str: ByteString): Message = throw new NotImplementedError()
  }

  test("getMessageLength should correctly parse first 4 bytes of message in to integer - simple") {
    val length = SimpleParser.getMessageLength(
      new ByteStringBuilder()
        .putBytes(Array.fill(3)(0.toByte))
        .putByte(1.toByte)
        .result()
    )
    length should be (1)
  }

  test("getMessageLength should correctly parse first 4 bytes of message in to integer - interesting") {
    val length = SimpleParser.getMessageLength(
      new ByteStringBuilder()
        .putBytes(Array.fill(2)(0.toByte))
        .putBytes(Array.fill(2)(1.toByte))
        .result()
    )
    length should be (257)
  }

  test("getMessageLength should throw IAE when length appears to be bigger than Int.MaxValue") {
    val caught = intercept[IllegalArgumentException] {
      SimpleParser.getMessageLength(
        new ByteStringBuilder()
          .putBytes(Array.fill(4)(170.toByte))
          .result()
      )
    }
    caught.getMessage should include ("Int.MaxValue")
  }

  test("getMessageLength should correctly parse first 4 bytes of message in to integer - complex") {
    val length = SimpleParser.getMessageLength(
      new ByteStringBuilder()
        .putBytes(Array.fill(2)(0.toByte))
        .putBytes(Array.fill(2)(170.toByte))
        .result()
    )
    length should be (0xAAAAL)
  }
}
