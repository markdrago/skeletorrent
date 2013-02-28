package bencoding

import bencoding.{BEncoder, BDecoder}
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import main.scala._
import akka.util.ByteString

class BEncoderTest extends FunSuite with ShouldMatchers {
  test("encodeList can create a simple bencoded list of integers") {
    val encoded = (new BEncoder).encodeList(List(1, 2))
    val expected = (new BDecoder).decodeItem("li1ei2ee")
    encoded should be (expected)
  }

  test("encodeList can create a bencoded list with strings and integers") {
    val encoded = (new BEncoder).encodeList(List(1, "two"))
    val expected = (new BDecoder).decodeItem("li1e3:twoe")
    encoded should be (expected)
  }

  test("encodeList can create a bencoded list with internal lists") {
    val encoder = new BEncoder
    val innerList = encoder.encodeList(List(1, 2))
    val outerList = encoder.encodeList(List("a", innerList, "b"))
    val expected = (new BDecoder).decodeItem("l1:ali1ei2ee1:be")
    outerList should be (expected)
  }

  test("encodeList can create a bencoded list with an internal map") {
    val encoder = new BEncoder
    val innerMap = encoder.encodeMap(Map("a" -> "b"))
    val outerList = encoder.encodeList(List("a", innerMap, "b"))
    val expected = (new BDecoder).decodeItem("l1:ad1:a1:be1:be")
    outerList should be (expected)
  }

  test("encodeList throws exception when creating a list with non-sensical element") {
    val caught = evaluating {
      (new BEncoder).encodeList(List(1, (new BEncoder)))
    } should produce [IllegalArgumentException]
    caught.getMessage should include ("Unexpected element type")
  }

  test("encodeMap can create a simple map from string to string") {
    val encoded = (new BEncoder).encodeMap(Map("a" -> "b"))
    val expected = (new BDecoder).decodeItem("d1:a1:be")
    encoded should be (expected)
  }

  test("encodeMap can create a map with string and integer values") {
    val encoded = (new BEncoder).encodeMap(Map("a" -> "b", "c" -> 3))
    val expected = (new BDecoder).decodeItem("d1:a1:b1:ci3ee")
    encoded should be (expected)
  }

  test("encodeMap can create a map with an inner map as a value") {
    val innerMap = (new BEncoder).encodeMap(Map("a" -> "b"))
    val outerMap = (new BEncoder).encodeMap(Map("outer" -> innerMap))
    val expected = (new BDecoder).decodeItem("d5:outerd1:a1:bee")
    outerMap should be (expected)
  }

  test("encodeMap can create a map with an inner list") {
    val innerList = (new BEncoder).encodeList(List("a", "b"))
    val outerMap = (new BEncoder).encodeMap(Map("outer" -> innerList))
    val expected = (new BDecoder).decodeItem("d5:outerl1:a1:bee")
    outerMap should be (expected)
  }

  test("complex encoding structure can be assembled") {
    val bencoder = new BEncoder
    val encoded = bencoder.encodeList(List(1, bencoder.encodeMap(Map("a" -> "b")), "c"))
    val expected = (new BDecoder).decodeItem("li1ed1:a1:be1:ce")
    encoded should be (expected)
  }

  test("encodeItem can encode a byte string") {
    val encoded = (new BEncoder).encodeItem(ByteString("abc"))
    val expected = (new BDecoder).decodeString(ByteString("3:abc"))
    encoded should be (expected)
  }

  test("encoding a map with an inner list works") {
    val encoded = (new BEncoder).encodeMap(
      Map("a" -> "b", "c" -> List(1, 2, 3))
    )
    val expected = (new BDecoder).decodeItem("d1:a1:b1:cli1ei2ei3eee")
    encoded should be (expected)
  }

  test("encoding a map with an inner map works") {
    val encoded = (new BEncoder).encodeMap(
      Map("a" -> "b", "c" -> Map("d" -> "e"))
    )
    val expected = (new BDecoder).decodeItem("d1:a1:b1:cd1:d1:eee")
    encoded should be (expected)
  }
}
