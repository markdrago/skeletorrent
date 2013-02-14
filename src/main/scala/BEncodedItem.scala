package main.scala

import scala.language.implicitConversions

/* inspired by the JsValue JSON stuff in the play framework */

sealed trait BEncodedItem {
  def encodedLength: Int
}

case class BEncodedString(val value: Seq[Byte]) extends BEncodedItem {
  override def encodedLength: Int = value.length + value.length.toString.length + 1
  override def toString = new String(value.toArray, "UTF-8")
}
object BEncodedString {
  def fromString(str: String) = new BEncodedString(str.getBytes("UTF-8"))
}

case class BEncodedInt(val value: Int) extends BEncodedItem {
  override def encodedLength: Int = value.toString.length + 2
}

case class BEncodedList(val value: List[BEncodedItem] = List()) extends BEncodedItem {
  override def encodedLength: Int = (2 /: value.map {e=>e.encodedLength}) {_+_}
  def apply(index: Int): BEncodedItem = value(index)
}

case class BEncodedMap(val value: Map[String, BEncodedItem]) extends BEncodedItem {
  override def encodedLength: Int = {
    return (2 /: value) ((acc, kv) => acc + BEncodedString.fromString(kv._1).encodedLength + kv._2.encodedLength)
  }

  def get(key: String): Option[BEncodedItem] = value.get(key)
}
