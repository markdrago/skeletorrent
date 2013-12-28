package utils

import akka.util.ByteString
import org.scalatest.{Matchers, FunSuite}

class UtilsTest extends FunSuite with Matchers {
  test("urlEncode can encode a ByteString properly") {
    val input = ByteString("hello there world@.")
    val expected = "hello+there+world%40."
    Utils.urlEncode(input) should be (expected)
  }

  test("bsToHex can convert a bytestring to hex properly") {
    val input = ByteString("abcd")
    val expected = "61 62 63 64"
    Utils.bsToHex(input) should be (expected)
  }
}
