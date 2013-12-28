package wire.message

import akka.util.{ByteStringBuilder, ByteString}
import java.nio.ByteOrder
import org.scalatest.{Matchers, FunSuite}

class KeepAliveTest extends FunSuite with Matchers {
  implicit val bo = ByteOrder.BIG_ENDIAN

  test("KeepAlive parser throws IAE with KeepAlive message that is too short") {
    val caught = intercept[IllegalArgumentException] {
      KeepAlive(ByteString())
    }
    caught.getMessage should include ("too short")
  }

  test("KeepAlive parser throws IAE with KeepAlive message with length != 0") {
    val caught = intercept[IllegalArgumentException] {
      KeepAlive(new ByteStringBuilder()
        .putInt(1)
        .result()
      )
    }
    caught.getMessage should include ("length of 0")
  }

  test("KeepAlive parser works on correctly formatted KeepAlive message") {
    val result = KeepAlive(new ByteStringBuilder()
      .putInt(0)
      .result()
    )
    result should be (KeepAlive())
  }

  test("KeepAlive serializer works") {
    val expected = new ByteStringBuilder()
      .putInt(0)
      .result()
    KeepAlive().serialize should be (expected)
  }
}
