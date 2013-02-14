package test.scala

import org.scalatest.{BeforeAndAfter, FunSuite}
import main.scala._
import javax.xml.bind.DatatypeConverter
import sun.font.TrueTypeFont

class BDecoderTest extends FunSuite with BeforeAndAfter {

  var bdecoder: BDecoder = _

  before {
    bdecoder = new BDecoder
  }

  test("decodeString can decode a simple bencoded string") {
    val result = bdecoder.decodeString("5:hello".getBytes("UTF-8"))
    expectResult("hello") { result.toString }
  }

  test("decodeString can decode a longer bencoded string with trailing characters") {
    val result = bdecoder.decodeString(("25:" + ("abcde" * 5) + "fghijkl").getBytes("UTF-8"))
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
    intercept[IllegalArgumentException] { bdecoder.decodeString("bogus string".getBytes("UTF-8")) }
  }

  test("decodeString throws illegal argument if given a string without a semicolon") {
    intercept[IllegalArgumentException] { bdecoder.decodeString("8heythere".getBytes("UTF-8")) }
  }

  test("decodeString throws illegal argument if given a string that is too short") {
    intercept[IllegalArgumentException] { bdecoder.decodeString("5:hey".getBytes("UTF-8")) }
  }

  test("checkStringFormat throws for empty sequence") {
    intercept[IllegalArgumentException] { bdecoder.checkStringFormat(Seq[Byte]()) }
  }

  test("checkStringFormat does not throw for simple match") {
    bdecoder.checkStringFormat("1:a".getBytes)
  }

  test("checkStringFormat does not throw for multi-character string length") {
    bdecoder.checkStringFormat("12:abcdefghijkl".getBytes)
  }

  test("checkStringFormat throws for leading alpha character") {
    intercept[IllegalArgumentException] { bdecoder.checkStringFormat("a:a".getBytes)}
  }

  test("checkStringFormat throws for leading :") {
    intercept[IllegalArgumentException] { bdecoder.checkStringFormat(":1a".getBytes)}
  }

  test("decodeInteger throws for empty sequence") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger(Seq[Byte]()) }
  }

  test("decodeInteger throws for truncated sequence without a trailing e") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("i1".getBytes("UTF-8"))}
  }

  test("decodeInteger throws illegal argument if given a string that does not begin with 'i'") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("h123e".getBytes("UTF-8")) }
  }

  test("decodeInteger throws illegal argument if given a string that does not end with 'e'") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("i123f".getBytes("UTF-8")) }
  }

  test("decodeInteger can decode an integer") {
    val result = bdecoder.decodeInteger("i12345e".getBytes("UTF-8"))
    expectResult(12345) { result.value }
  }

  test("decodeInteger can decode a 0") {
    val result = bdecoder.decodeInteger("i0e".getBytes("UTF-8"))
    expectResult(0) { result.value }
  }

  test("decodeInteger can decode a negative number") {
    val result = bdecoder.decodeInteger("i-25e".getBytes("UTF-8"))
    expectResult(-25) { result.value }
  }

  test("decodeInteger can decode an int when there are extra characters at the end") {
    val result = bdecoder.decodeInteger("i123eabc".getBytes("UTF-8"))
    expectResult(123) { result.value }
  }

  test("decodeInteger throws illegal argument for negative 0") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("i-0e".getBytes("UTF-8")) }
  }

  test("decodeInteger throws illegal argument with leading zero but not equal to 0") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("i02e".getBytes("UTF-8")) }
  }

  test("decodeList throws illegal argument if the encoded string does not begin with 'l") {
    intercept[IllegalArgumentException] { bdecoder.decodeList("notaliste".getBytes("UTF-8")) }
  }

  test("decodeList can decode list with one element") {
    val result = bdecoder.decodeList("l5:helloe".getBytes("UTF-8"))
    expectResult("hello") { result.value.head.asInstanceOf[BEncodedString].toString }
  }

  test("decodeList can decode list with more than one element") {
    val encoded = "l5:helloi123e3:byee"
    val result = bdecoder.decodeList(encoded.getBytes("UTF-8"))
    expectResult("hello") { result.value(0).asInstanceOf[BEncodedString].toString }
    expectResult(123) { result.value(1).asInstanceOf[BEncodedInt].value }
    expectResult("bye") { result.value(2).asInstanceOf[BEncodedString].toString }
  }

  test("decodeMap throws illegal argument if given string that does not begin with 'd'") {
    intercept[IllegalArgumentException] { bdecoder.decodeMap("i123e".getBytes("UTF-8")) }
  }

  test("decodeMap throws illegal argument if not given even # of items") {
    intercept[IllegalArgumentException] { bdecoder.decodeMap("di1e1:ai2ee".getBytes("UTF-8"))}
  }

  test("decodeMap can decode a simple map") {
    val encoded = "d1:a1:be"
    val result = bdecoder.decodeMap(encoded.getBytes("UTF-8"))
    expectResult(1) {result.value.size}

    val expected_value = new BEncodedString(Seq[Byte] (98))
    expectResult(expected_value) { result.value.get("a").get }
  }

  test("decodeMap can decode a complex map") {
    val encoded = "d1:k" + "d2:ik1:ae" + "e"
    val result = bdecoder.decodeMap(encoded.getBytes("UTF-8"))
    expectResult(1) { result.value.size }

    val inner = result.value.get("k").get.asInstanceOf[BEncodedMap]
    expectResult(1) { inner.value.size }

    val expected_inner_value = new BEncodedString(Seq[Byte] (97))
    expectResult(expected_inner_value) { inner.value.get("ik").get }
  }

  test("decodeItem throws illegal argument if given non-sensical encoded string") {
    intercept[IllegalArgumentException] { bdecoder.decodeItem("blah".getBytes("UTF-8")) }
  }

  test("decodeItem parses a simple real world metainfo file") {
    val decoded = bdecoder.decodeItem(get_metainfo_file_contents)
    val decodedMap = decoded.asInstanceOf[BEncodedMap]

    expectResult(false) { decodedMap.get("announce").isEmpty }
    val actual_announce = decodedMap.get("announce").get.toString
    expectResult("http://www.legaltorrents.com:7070/announce") { actual_announce }
  }

  def get_metainfo_file_contents: Seq[Byte] = {
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
    DatatypeConverter.parseBase64Binary(encoded)
  }
}
