package bencoding.items

import scala.language.reflectiveCalls
import org.scalatest.{FunSuite, Matchers}
import akka.util.ByteString

class BEncodedItemTest extends FunSuite with Matchers {

  def fixture =
    new {
      //*_a == *_b, but *_a != *_c
      val int_a = new BEncodedInt(1)
      val int_b = new BEncodedInt(1)
      val int_c = new BEncodedInt(2)
      val str_a = BEncodedString.fromString("a")
      val str_b = BEncodedString.fromString("a")
      val str_c = BEncodedString.fromString("b")
      val list_a = new BEncodedList(List(int_a, str_a))
      val list_b = new BEncodedList(List(int_b, str_b))
      val list_c = new BEncodedList(List(int_c, str_c))
      val map_a = new BEncodedMap(Map("a" -> str_a))
      val map_b = new BEncodedMap(Map("a" -> str_b))
      val map_c = new BEncodedMap(Map("c" -> str_c))
    }

  test("BEncodedInt equals method works") {
    val f = fixture
    assert(f.int_a.equals(f.int_b))
    assert(! f.int_a.equals(f.int_c))
  }

  test("BEncodedInt hashCode method works") {
    val f = fixture
    assert(f.int_a.hashCode.equals(f.int_b.hashCode))
    assert(! f.int_a.hashCode.equals(f.int_c.hashCode))
  }

  test("BEncodedInt encodedLength works") {
    assertResult(7) { new BEncodedInt(12345).encodedLength }
  }

  test("BEncodedInt encodedLength works with a negative number") {
    assertResult(5) { new BEncodedInt(-25).encodedLength }
  }

  test("BEncodedInt serialize works for positive number") {
    verifyBEncodedIntProducesExpectedResultWhenSerialized(5, "i5e")
  }

  test("BEncodedInt serialize works for negative number") {
    verifyBEncodedIntProducesExpectedResultWhenSerialized(-5, "i-5e")
  }

  test("BEncodedInt serialize works for zero") {
    verifyBEncodedIntProducesExpectedResultWhenSerialized(0, "i0e")
  }

  def verifyBEncodedIntProducesExpectedResultWhenSerialized(input: Int, expected: String) {
    new BEncodedInt(input).serialize should be (ByteString(expected))
  }

  test("BEncodedString encodedLength works") {
    assertResult(7) { BEncodedString.fromString("hello").encodedLength }
  }

  test("BEncodedString encodedLength works for multi-byte utf8 chars") {
    assertResult(13) { BEncodedString.fromString("lambda: λ").encodedLength }
  }

  test("BEncodedString equals method works") {
    val f = fixture
    assert(f.str_a.equals(f.str_b))
    assert(! f.str_a.equals(f.str_c))
  }

  test("BEncodedString hashCode method works") {
    val f = fixture
    assert(f.str_a.hashCode.equals(f.str_b.hashCode))
    assert(! f.str_a.hashCode.equals(f.str_c.hashCode))
  }

  test("BEncodedString serialize works for regular string") {
    verifyBEncodedStringProducesExpectedResultWhenSerialized("abc", 3)
  }

  test("BEncodedString serialize works for byte sequence w/ multi-byte UTF-8 char") {
    verifyBEncodedStringProducesExpectedResultWhenSerialized("lambda: λ", 10)
  }

  def verifyBEncodedStringProducesExpectedResultWhenSerialized(input: String, len: Int) {
    BEncodedString.fromString(input).serialize should be (ByteString((len.toString + ":" + input)))
  }

  test("BEncodedList equals method works") {
    val f = fixture
    assert(f.list_a.equals(f.list_b))
    assert(! f.list_a.equals(f.list_c))
  }

  test("BEncodedList hashCode method works") {
    val f = fixture
    assert(f.list_a.hashCode.equals(f.list_b.hashCode))
    assert(! f.list_a.hashCode.equals(f.list_c.hashCode))
  }

  test("BEncodedList encodedLength works") {
    val f = fixture
    assertResult(8) { f.list_a.encodedLength }
  }

  test("BEncodedList can have its elements retrieved") {
    val f = fixture
    assertResult(f.int_a) { f.list_a(0) }
    assertResult(f.str_a) { f.list_a(1) }
  }

  test("BEncodedList serialize works for a simple mixed list") {
    val f = fixture
    f.list_a.serialize should be (ByteString("li1e1:ae"))
  }

  test("BEncodedList serialize works for a nested list") {
    val f = fixture
    val l = new BEncodedList(List(f.int_c, f.list_a))
    l.serialize should be (ByteString("li2eli1e1:aee"))
  }

  test("BEncodedList is mappable") {
    val f = fixture
    f.list_a.map(_.toString)
  }

  test("BEncodedMap equals method works") {
    val f = fixture
    assert(f.map_a.equals(f.map_b))
    assert(! f.map_a.equals(f.map_c))
  }

  test("BEncodedMap hashCode method works") {
    val f = fixture
    assert(f.map_a.hashCode().equals(f.map_b.hashCode()))
    assert(! f.map_a.hashCode().equals(f.map_c.hashCode()))
  }

  test("BEncodedMap encodedLength works") {
    val f = fixture
    assertResult(8) { f.map_a.encodedLength }
  }

  test("BEncodedMap get works") {
    val f = fixture
    assertResult(f.str_a) { f.map_a.get("a").get }
  }

  test("BEncodedMap serialize works for a simple dictionary") {
    val f = fixture
    f.map_a.serialize should be (ByteString("d1:a1:ae"))
  }

  test("BEncodedMap serialize works for a nested dictionary") {
    val f = fixture
    val d = BEncodedMap(Map("A" -> f.map_c))
    d.serialize should be (ByteString("d1:Ad1:c1:bee"))
  }

  test("BEncodedMap is mappable") {
    val f = fixture
    f.map_a.map(_.toString())
  }

  test("BEncodedMap can have items added to it") {
    val f = fixture
    val newMap = f.map_a + ("new" -> BEncodedInt(3))
    newMap.iterator.length should be (2)
    newMap.get("new").get should be (BEncodedInt(3))
  }

  test("BEncodedMap can have items removed from it") {
    val f = fixture
    val newMap = f.map_a - "a"
    newMap.iterator.length should be (0)
  }

  test("BEncodedItem without overriden toInt throws exception") {
    val f = fixture
    evaluating { f.str_a.toInt } should produce [IllegalStateException]
  }

  test("BEncodedItem without overriden get method throws exception") {
    val f = fixture
    evaluating { f.str_a.get("hello") } should produce [IllegalStateException]
  }

  test("serialization succeeds for a highly nested structure") {
    val f = fixture
    val l = BEncodedList(List(f.int_a, f.str_a, f.map_a))
    val d = BEncodedMap(Map("a" -> l, "b" -> f.list_a))
    d.serialize should be (ByteString("d1:ali1e1:ad1:a1:aee1:bli1e1:aee"))
  }
}