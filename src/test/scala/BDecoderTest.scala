package test.scala

import org.scalatest.{BeforeAndAfter, FunSuite}
import main.scala._

class BDecoderTest extends FunSuite with BeforeAndAfter {

  var bdecoder: BDecoder = _

  before {
    bdecoder = new BDecoder
  }

  test("decodeString can decode a simple bencoded string") {
    val result = bdecoder.decodeString("5:hello".getBytes("UTF-8"))
    expectResult("hello") { result.asString }
  }

  test("decodeString can decode a longer bencoded string with trailing characters") {
    val result = bdecoder.decodeString(("25:" + ("abcde" * 5) + "fghijkl").getBytes("UTF-8"))
    expectResult("abcde" * 5) { result.asString }
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
    expectResult("hello") { result.value.head.asInstanceOf[BEncodedString].asString }
  }

  test("decodeList can decode list with more than one element") {
    val encoded = "l5:helloi123e3:byee"
    val result = bdecoder.decodeList(encoded.getBytes("UTF-8"))
    expectResult("hello") { result.value(0).asInstanceOf[BEncodedString].asString }
    expectResult(123) { result.value(1).asInstanceOf[BEncodedInt].value }
    expectResult("bye") { result.value(2).asInstanceOf[BEncodedString].asString }
  }

  test("decodeMap throws illegal argument if given string that does not begin with 'd'") {
    intercept[IllegalArgumentException] { bdecoder.decodeMap("i123e".getBytes("UTF-8")) }
  }

  test("decodeMap throws illegal argument if not given even # of items") {
    intercept[IllegalArgumentException] { bdecoder.decodeMap("di1e1:ai2ee".getBytes("UTF-8"))}
  }

  test("decodeMap can decode a simple map") {
    val encoded = "di1e1:ae"
    val result = bdecoder.decodeMap(encoded.getBytes("UTF-8"))
    expectResult(1) {result.value.size}

    val expected_key = new BEncodedInt(1)
    val expected_value = new BEncodedString(Seq[Byte] (97))
    expectResult(expected_value) { result.value.get(expected_key).get }
  }

  test("decodeMap can decode a complex map") {
    val encoded = "di1e" + "di2e1:ae" + "e"
    val result = bdecoder.decodeMap(encoded.getBytes("UTF-8"))
    expectResult(1) { result.value.size }

    val expected_key = new BEncodedInt(1)
    val inner = result.value.get(expected_key).get.asInstanceOf[BEncodedMap]
    expectResult(1) { inner.value.size }

    val expected_inner_key = new BEncodedInt(2)
    val expected_inner_value = new BEncodedString(Seq[Byte] (97))
    expectResult(expected_inner_value) { inner.value.get(expected_inner_key).get }
  }

  test("decodeItem throws illegal argument if given non-sensical encoded string") {
    intercept[IllegalArgumentException] { bdecoder.decodeItem("blah".getBytes("UTF-8")) }
  }
}
