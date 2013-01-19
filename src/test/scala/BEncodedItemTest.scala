package test.scala

import org.scalatest.{BeforeAndAfter, FunSuite}
import main.scala._

class BEncodedItemTest extends FunSuite with BeforeAndAfter {

  def fixture =
    new {
      //*_a == *_b, but *_a != *_c
      val int_a = new BEncodedInt(1)
      val int_b = new BEncodedInt(1)
      val int_c = new BEncodedInt(2)
      val str_a = new BEncodedString("a")
      val str_b = new BEncodedString("a")
      val str_c = new BEncodedString("b")
      val list_a = new BEncodedList(List(int_a, str_a))
      val list_b = new BEncodedList(List(int_b, str_b))
      val list_c = new BEncodedList(List(int_c, str_c))
      val map_a = new BEncodedMap(Map(int_a -> str_a))
      val map_b = new BEncodedMap(Map(int_b -> str_b))
      val map_c = new BEncodedMap(Map(int_c -> str_c))
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
    expectResult(7) { new BEncodedInt(12345).encodedLength }
  }

  test("BEncodeInt encodedLength works with a negative number") {
    expectResult(5) { new BEncodedInt(-25).encodedLength }
  }

  test("BEncodeInt implicit adding works") {
    val f = fixture
    expectResult(3) { 2 + f.int_a }
  }

  test("BEncodedString encodedLength works") {
    expectResult(7) { new BEncodedString("hello").encodedLength }
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

  test("BEncodedString implicit length check works") {
    val f = fixture
    expectResult(1) { f.str_a.length }
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
    expectResult(8) { f.list_a.encodedLength }
  }

  test("BEncodedList implicit size check works") {
    val f = fixture
    expectResult(2) { f.list_a.size }
  }

  test("BEncodedMap equals method works") {
    val f = fixture
    assert(f.map_a.equals(f.map_b))
    assert(! f.map_a.equals(f.map_c))
  }

  test("BEncodedMap hashCode method works") {
    val f = fixture
    assert(f.map_a.hashCode.equals(f.map_b.hashCode))
    assert(! f.map_a.hashCode.equals(f.map_c.hashCode))
  }

  test("BEncodedMap encodedLength works") {
    val f = fixture
    expectResult(8) { f.map_a.encodedLength }
  }

  test("BEncodedMap implicit get works") {
    val f = fixture
    expectResult(f.str_a) { f.map_a.get(f.int_a).get }
  }
}