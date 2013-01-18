package test.scala

import org.scalatest.{BeforeAndAfter, FunSuite}
import main.scala.{BDecoder, BEncodedInt, BEncodedString, BEncodedMap}

class BDecoderTest extends FunSuite with BeforeAndAfter {

  var bdecoder: BDecoder = _

  before {
    bdecoder = new BDecoder
  }

  test("decodeString can decode a simple bencoded string") {
    val result = bdecoder.decodeString("5:hello")
    expectResult("hello") { result.value }
  }

  test("decodeString can decode a longer bencoded string with trailing characters") {
    val result = bdecoder.decodeString("25:" + ("abcde" * 5) + "fghijkl")
    expectResult("abcde" * 5) { result.value }
    expectResult(28) { result.encodedLength }
  }

  test("decodeString throws illegal argument if given a string not starting with digit"){
    intercept[IllegalArgumentException] { bdecoder.decodeString("bogus string") }
  }

  test("decodeString throws illegal argument if given a string without a semicolon"){
    intercept[IllegalArgumentException] { bdecoder.decodeString("8heythere") }
  }

  test("decodeString throws illegal argument if given a string that is too short"){
    intercept[IllegalArgumentException] { bdecoder.decodeString("5:hey") }
  }

  test("decodeInteger throws illegal argument if given a string that does not begin with 'i'") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("h123e") }
  }

  test("decodeInteger throws illegal argument if given a string that does not end with 'e'") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("i123f") }
  }

  test("decodeInteger can decode an integer") {
    val result = bdecoder.decodeInteger("i12345e")
    expectResult(12345) { result.value }
  }

  test("decodeInteger can decode a 0") {
    val result = bdecoder.decodeInteger("i0e")
    expectResult(0) { result.value }
  }

  test("decodeInteger can decode a negative number") {
    val result = bdecoder.decodeInteger("i-25e")
    expectResult(-25) { result.value }
  }

  test("decodeInteger can decode an int when there are extra characters at the end") {
    val result = bdecoder.decodeInteger("i123eabc")
    expectResult(123) { result.value }
  }

  test("decodeInteger throws illegal argument for negative 0") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("i-0e") }
  }

  test("decodeInteger throws illegal argument with leading zero but not equal to 0") {
    intercept[IllegalArgumentException] { bdecoder.decodeInteger("i02e") }
  }

  test("decodeList throws illegal argument if the encoded string does not begin with 'l") {
    intercept[IllegalArgumentException] { bdecoder.decodeList("notaliste") }
  }

  test("decodeList can decode list with one element") {
    val result = bdecoder.decodeList("l5:helloe")
    expectResult("hello") { result.value.head.value }
  }

  test("decodeList can decode list with more than one element") {
    val encoded = "l5:helloi123e3:byee"
    val result = bdecoder.decodeList(encoded)
    expectResult("hello") { result.value(0).value }
    expectResult(123) { result.value(1).value }
    expectResult("bye") { result.value(2).value }
  }

  test("decodeMap throws illegal argument if given string that does not begin with 'd'") {
    intercept[IllegalArgumentException] { bdecoder.decodeMap("i123e") }
  }

  test("decodeMap throws illegal argument if not given even # of items") {
    intercept[IllegalArgumentException] { bdecoder.decodeMap("di1e1:ai2ee")}
  }

  test("decodeMap can decode a simple map") {
    val encoded = "di1e1:ae"
    val result = bdecoder.decodeMap(encoded)
    expectResult(1) {result.value.size}

    val expected_key = new BEncodedInt(1)
    val expected_value = new BEncodedString("a")
    expectResult(expected_value) { result.value.get(expected_key).get }
  }

  test("decodeMap can decode a complex map") {
    val encoded = "di1e" + "di2e1:ae" + "e"
    val result = bdecoder.decodeMap(encoded)
    expectResult(1) { result.value.size }

    val expected_key = new BEncodedInt(1)
    val inner = result.value.get(expected_key).get.asInstanceOf[BEncodedMap]
    expectResult(1) { inner.value.size }

    val expected_inner_key = new BEncodedInt(2)
    val expected_inner_value = new BEncodedString("a")
    expectResult(expected_inner_value) { inner.value.get(expected_inner_key).get }
  }

  test("decodeItem throws illegal argument if given non-sensical encoded string") {
    intercept[IllegalArgumentException] { bdecoder.decodeItem("blah") }
  }
}
