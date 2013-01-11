package test.scala

import org.scalatest.{BeforeAndAfter, FunSuite}
import main.scala.BEncode

class BEncodeTest extends FunSuite with BeforeAndAfter {

  var bencode: BEncode = _

  before {
    bencode = new BEncode
  }

  test("decodeString can decode a simple bencoded string") {
    val result = bencode.decodeString("5:hello")
    expectResult(("hello", 7)) { result }
  }

  test("decodeString can decode a longer bencoded string with trailing characters") {
    val result = bencode.decodeString("25:" + ("abcde" * 5) + "fghijkl")
    expectResult(("abcde" * 5, 28)) { result }
  }

  test("decodeString throws illegal argument if given a string not starting with digit"){
    intercept[IllegalArgumentException] { bencode.decodeString("bogus string") }
  }

  test("decodeString throws illegal argument if given a string without a semicolon"){
    intercept[IllegalArgumentException] { bencode.decodeString("8heythere") }
  }

  test("decodeString throws illegal argument if given a string that is too short"){
    intercept[IllegalArgumentException] { bencode.decodeString("5:hey") }
  }

  test("decodeInteger throws illegal argument if given a string that does not begin with 'i'") {
    intercept[IllegalArgumentException] { bencode.decodeInteger("h123e") }
  }

  test("decodeInteger throws illegal argument if given a string that does not end with 'e'") {
    intercept[IllegalArgumentException] { bencode.decodeInteger("i123f") }
  }

  test("decodeInteger can decode an integer") {
    val encodedInt = "i12345e"
    expectResult((12345, 7)) {bencode.decodeInteger(encodedInt)}
  }

  test("decodeInteger can decode a 0") {
    val encodedInt = "i0e"
    expectResult((0, 3)) {bencode.decodeInteger(encodedInt)}
  }

  test("decodeInteger can decode a negative number") {
    val encodedInt = "i-25e"
    expectResult((-25, 5)) {bencode.decodeInteger(encodedInt)}
  }

  test("decodeInteger throws illegal argument for negative 0") {
    intercept[IllegalArgumentException] { bencode.decodeInteger("i-0e") }
  }

  test("decodeInteger throws illegal argument with leading zero but not equal to 0") {
    intercept[IllegalArgumentException] { bencode.decodeInteger("i02e") }
  }
}
