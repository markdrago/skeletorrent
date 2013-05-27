package wire.message

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import akka.util.{ByteStringBuilder, ByteString}
import java.nio.ByteOrder

class HaveTest extends FunSuite with ShouldMatchers {
  implicit val bo = ByteOrder.BIG_ENDIAN

  def haveByteStringWithPiece(index: Int) = {
    new ByteStringBuilder()
      .putInt(5)
      .putByte(4.toByte)
      .putInt(index)
      .result()
  }

  test("Have parser throws IAE if length is < 5") {
    val caught = intercept[IllegalArgumentException] {
      Have(ByteString())
    }
    caught.getMessage should include ("too short")
  }

  test("Have parser throws IAE if indicated length is != 5") {
    val caught = intercept[IllegalArgumentException] {
      Have(new ByteStringBuilder()
        .putInt(4)
        .putBytes(Array.fill(4)(0.toByte))
        .result()
      )
    }
    caught.getMessage should include ("fixed length of 5")
  }

  test("Have parser throws IAE if message type is not 4") {
    val caught = intercept[IllegalArgumentException] {
      Have(new ByteStringBuilder()
        .putInt(5)
        .putByte(3.toByte)
        .putBytes(Array.fill(4)(0.toByte))
        .result()
      )
    }
    caught.getMessage should include ("message type 4")
  }

  test("Have parser works for finding piece 0") {
    Have(haveByteStringWithPiece(0)) should be (new Have(0))
  }

  test("Have parser works for finding large piece number") {
    Have(haveByteStringWithPiece(1023)) should be (new Have(1023))
  }

  test("Have serializer works when piece is 0") {
    (new Have(0)).serialize should be (haveByteStringWithPiece(0))
  }

  test("Have serializer works with larger piece number") {
    (new Have(1023)).serialize should be (haveByteStringWithPiece(1023))
  }
}
