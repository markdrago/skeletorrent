package wire.message

import java.nio.ByteOrder
import akka.util.{ByteStringBuilder, ByteString}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

trait SimpleMessageTest extends FunSuite with ShouldMatchers {
  def messageName: String
  def messageType: Byte
  def messageParser(str: ByteString): Message
  def messageFactory: Message

  implicit val bo = ByteOrder.BIG_ENDIAN

  test(s"$messageName parser throws IAE with $messageName message that is too short") {
    val caught = intercept[IllegalArgumentException] {
      messageParser(ByteString())
    }
    caught.getMessage should include ("too short")
  }

  test(s"$messageName parser throws IAE with $messageName message with length != 5") {
    val caught = intercept[IllegalArgumentException] {
      messageParser(new ByteStringBuilder()
        .putInt(4)
        .putInt(0)
        .result()
      )
    }
    caught.getMessage should include ("size of 1")
  }

  test(s"$messageName parser throws IAE with $messageName message containing type != $messageType") {
    val bogusType = if (messageType == 0.toByte) 1.toByte else 0.toByte
    val caught = intercept[IllegalArgumentException] {
      messageParser(new ByteStringBuilder()
        .putInt(1)
        .putByte(bogusType)
        .result()
      )
    }
    caught.getMessage should include (s"requires type of $messageType")
  }

  test(s"$messageName parser works on correctly formatted $messageName message") {
    val result = messageParser(new ByteStringBuilder()
      .putInt(1)
      .putByte(messageType)
      .result()
    )
    result should be (messageFactory)
  }

  test(s"$messageName serializer works") {
    val expected = new ByteStringBuilder()
      .putInt(1)
      .putByte(messageType)
      .result()
    messageFactory.serialize should be (expected)
  }
}
