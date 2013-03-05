package utils

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import metainfo.MetaInfo
import javax.xml.bind.DatatypeConverter
import akka.util.ByteString

class UtilsTest extends FunSuite with ShouldMatchers {
  test("urlEncode can encode a ByteString properly") {
    val input = ByteString("hello there world@.")
    val expected = "hello+there+world%40."
    Utils.urlEncode(input) should be (expected)
  }
}
