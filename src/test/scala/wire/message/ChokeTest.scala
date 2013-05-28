package wire.message

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import akka.util.{ByteStringBuilder, ByteString}
import java.nio.ByteOrder

class ChokeTest extends FunSuite with ShouldMatchers {
  implicit val bo = ByteOrder.BIG_ENDIAN

  test("Choke parser throws IAE with Choke message that is too short") {
    val caught = intercept[IllegalArgumentException] {
      Choke(ByteString())
    }
    caught.getMessage should include ("too short")
  }

  test("Choke parser throws IAE with Choke message with length != 5") {
    val caught = intercept[IllegalArgumentException] {
      Choke(new ByteStringBuilder()
        .putInt(4)
        .putInt(0)
        .result()
      )
    }
    caught.getMessage should include ("size of 1")
  }

  test("Choke parser throws IAE with Choke message containing type != 0") {
    val caught = intercept[IllegalArgumentException] {
      Choke(new ByteStringBuilder()
        .putInt(1)
        .putByte(1.toByte)
        .result()
      )
    }
    caught.getMessage should include ("requires type of 0")
  }

  test("Choke parser works on correctly formatted Choke message") {
    val result = Choke(new ByteStringBuilder()
      .putInt(1)
      .putByte(0.toByte)
      .result()
    )
    result should be (Choke())
  }

  test("Choke serializer works") {
    val expected = new ByteStringBuilder()
      .putInt(1)
      .putByte(0.toByte)
      .result()
    Choke().serialize should be (expected)
  }
}
