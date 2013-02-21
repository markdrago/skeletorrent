package test.scala

import org.scalatest.{BeforeAndAfter, FunSuite}
import main.scala._
import javax.xml.bind.DatatypeConverter
import akka.util.ByteString

class BDecoderTest extends FunSuite with BeforeAndAfter {

  var bdecoder: BDecoder = _

  before {
    bdecoder = new BDecoder
  }

  def bs(s: String): ByteString = ByteString(s)

  test("decodeString can decode a simple bencoded string") {
    val result = bdecoder.decodeString(ByteString("5:hello"))
    expectResult("hello") { result.toString }
  }

  test("decodeString can decode a longer bencoded string with trailing characters") {
    val result = bdecoder.decodeString(bs("25:" + ("abcde" * 5) + "fghijkl"))
    expectResult("abcde" * 5) { result.toString }
    expectResult(28) { result.encodedLength }
  }

  test("decodeString can decode a byte sequence which is not a utf8 string") {
    val header = "5:".getBytes("UTF-8").toSeq
    val expected = Seq(0, 240, 221, 32, 17).map(_.toByte)
    val result = bdecoder.decodeString(header union expected)
    expectResult(expected.length) { result.value.length }
    expectResult(expected) { result.value }
    expectResult(expected.length + 2) { result.encodedLength }
  }

  test("decodeString throws illegal argument if given a string not starting with digit") {
    intercept[IllegalArgumentException] { bdecoder.decodeString(bs("bogus string")) }
  }

  test("decodeString throws illegal argument if given a string without a semicolon") {
    intercept[IllegalArgumentException] { bdecoder.decodeString(bs("8heythere")) }
  }

  test("decodeString throws illegal argument if given a string that is too short") {
    intercept[IllegalArgumentException] { bdecoder.decodeString(bs("5:hey")) }
  }

  test("checkStringFormat throws for empty sequence") {
    intercept[IllegalArgumentException] { bdecoder.checkStringFormat(ByteString()) }
  }

  test("checkStringFormat does not throw for simple match") {
    bdecoder.checkStringFormat(bs("1:a"))
  }

  test("checkStringFormat does not throw for multi-character string length") {
    bdecoder.checkStringFormat(bs("12:abcdefghijkl"))
  }

  test("checkStringFormat throws for leading alpha character") {
    intercept[IllegalArgumentException] { bdecoder.checkStringFormat(bs("a:a"))}
  }

  test("checkStringFormat throws for leading :") {
    intercept[IllegalArgumentException] { bdecoder.checkStringFormat(bs(":1a"))}
  }

  test("decodeInteger throws for empty sequence") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger(ByteString()) }
  }

  test("decodeInteger throws for truncated sequence without a trailing e") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger(bs("i1"))}
  }

  test("decodeInteger throws illegal argument if given a string that does not begin with 'i'") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger(bs("h123e")) }
  }

  test("decodeInteger throws illegal argument if given a string that does not end with 'e'") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger(bs("i123f")) }
  }

  test("decodeInteger can decode an integer") {
    val result = bdecoder.decodeInteger(bs("i12345e"))
    expectResult(12345) { result.value }
  }

  test("decodeInteger can decode a 0") {
    val result = bdecoder.decodeInteger(bs("i0e"))
    expectResult(0) { result.value }
  }

  test("decodeInteger can decode a negative number") {
    val result = bdecoder.decodeInteger(bs("i-25e"))
    expectResult(-25) { result.value }
  }

  test("decodeInteger can decode an int when there are extra characters at the end") {
    val result = bdecoder.decodeInteger(bs("i123eabc"))
    expectResult(123) { result.value }
  }

  test("decodeInteger throws illegal argument for negative 0") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger(bs("i-0e")) }
  }

  test("decodeInteger throws illegal argument with leading zero but not equal to 0") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger(bs("i02e")) }
  }

  test("decodeList throws illegal argument if the encoded string does not begin with 'l") {
    intercept[IllegalArgumentException] { bdecoder.decodeList(bs("notaliste")) }
  }

  test("decodeList can decode list with one element") {
    val result = bdecoder.decodeList(bs("l5:helloe"))
    expectResult("hello") { result.value.head.asInstanceOf[BEncodedString].toString }
  }

  test("decodeList can decode list with more than one element") {
    val encoded = "l5:helloi123e3:byee"
    val result = bdecoder.decodeList(bs(encoded))
    expectResult("hello") { result.value(0).asInstanceOf[BEncodedString].toString }
    expectResult(123) { result.value(1).asInstanceOf[BEncodedInt].value }
    expectResult("bye") { result.value(2).asInstanceOf[BEncodedString].toString }
  }

  test("decodeMap throws illegal argument if given string that does not begin with 'd'") {
    intercept[IllegalArgumentException] { bdecoder.decodeMap(bs("i123e")) }
  }

  test("decodeMap throws illegal argument if not given even # of items") {
    intercept[IllegalArgumentException] { bdecoder.decodeMap(bs("di1e1:ai2ee"))}
  }

  test("decodeMap can decode a simple map") {
    val encoded = bs("d1:a1:be")
    val result = bdecoder.decodeMap(encoded)
    expectResult(1) {result.value.size}

    val expected_value = new BEncodedString(bs("b"))
    expectResult(expected_value) { result.value.get("a").get }
  }

  test("decodeMap can decode a complex map") {
    val encoded = bs("d1:k" + "d2:ik1:ae" + "e")
    val result = bdecoder.decodeMap(encoded)
    expectResult(1) { result.value.size }

    val inner = result.value.get("k").get.asInstanceOf[BEncodedMap]
    expectResult(1) { inner.value.size }

    val expected_inner_value = new BEncodedString(bs("a"))
    expectResult(expected_inner_value) { inner.value.get("ik").get }
  }

  test("decodeItem throws illegal argument if given non-sensical encoded string") {
    intercept[IllegalArgumentException] { bdecoder.decodeItem("blah") }
  }

  test("decodeItem parses a simple real world metainfo file") {
    val decoded = bdecoder.decodeItem(get_metainfo_file_contents)
    val decodedMap = decoded.asInstanceOf[BEncodedMap]

    expectResult(false) { decodedMap.get("announce").isEmpty }
    val actual_announce = decodedMap.get("announce").get.toString
    expectResult("http://www.legaltorrents.com:7070/announce") { actual_announce }
  }

  def get_metainfo_file_contents: ByteString = {
    /* contents:
     * d8:announce42:http://www.legaltorrents.com:7070/announce13:creation datei1081312084e
     * 4:infod6:lengthi2133210e4:name15:freeculture.zip12:piece lengthi262144e6:pieces180:
     * <binary sha1sums>
     * ee
     */
    val encoded =
      "ZDg6YW5ub3VuY2U0MjpodHRwOi8vd3d3LmxlZ2FsdG9ycmVudHMuY29tOjcwNzAvYW5ub3VuY2Ux" +
        "MzpjcmVhdGlvbiBkYXRlaTEwODEzMTIwODRlNDppbmZvZDY6bGVuZ3RoaTIxMzMyMTBlNDpuYW1l" +
        "MTU6ZnJlZWN1bHR1cmUuemlwMTI6cGllY2UgbGVuZ3RoaTI2MjE0NGU2OnBpZWNlczE4MDrtiec4" +
        "Sw00D6X/+hnRoA2I51S+AFBcbtsV0weqjkY4kfTSUw6N0TkKBHbzlzuhabVhAx7MQyh1GkwAlOHy" +
        "MUlEH0osleKnyMGAS73zefOd3E5DVssVKnpLa8XMHJB5q9Hk98QiXmhc/GNWL4Gu1pEaD5LIMCbU" +
        "2Gc8oiM0J8BrIMLO0Ca3uEkys8EmVdvvSeAFc3OsidBWJ1jzkaX33qtSst1ZrxLjRBuLU5P/jXbe" +
        "a8GRD4RlZQ=="
    ByteString(DatatypeConverter.parseBase64Binary(encoded))
  }
}
