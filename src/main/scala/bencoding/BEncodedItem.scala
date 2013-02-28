package bencoding

import akka.util.ByteString
import scala.language.implicitConversions

/* inspired by the JsValue JSON stuff in the play framework */

sealed trait BEncodedItem {
  def encodedLength: Int
  def serialize: ByteString
  def get(key: String): Option[BEncodedItem] = {
    throw new IllegalStateException("BEncodedItem is not a map: " + this.toString)
  }
  def toInt: Int = {
    throw new IllegalStateException("BEncodedItem is not an integer: " + this.toString)
  }
}

case class BEncodedString(value: ByteString) extends BEncodedItem {
  override def encodedLength: Int = value.length + value.length.toString.length + 1
  override def toString = new String(value.toArray, "UTF-8")
  override def serialize = ByteString(value.length.toString + ":") ++ value
}
object BEncodedString {
  def fromString(str: String) = new BEncodedString(ByteString(str))
}

case class BEncodedInt(value: Int) extends BEncodedItem {
  override def encodedLength: Int = value.toString.length + 2
  override def toInt: Int = value
  override def serialize = ByteString("i" + value.toString + "e")
}

case class BEncodedList(value: List[BEncodedItem] = List()) extends BEncodedItem with Traversable[BEncodedItem] {
  def length = value.length
  override def encodedLength: Int = (2 /: value.map {e=>e.encodedLength}) {_+_}
  def apply(index: Int): BEncodedItem = value(index)
  override def serialize = ByteString("l") ++ value.flatMap(_.serialize) ++ ByteString("e")
  override def foreach[A](f: BEncodedItem => A) = { value.foreach(f) }
}

case class BEncodedMap(value: Map[String, BEncodedItem]) extends BEncodedItem with Map[String, BEncodedItem] {
  override def encodedLength: Int = {
    (2 /: value) ((acc, kv) => acc + BEncodedString.fromString(kv._1).encodedLength + kv._2.encodedLength)
  }
  override def get(key: String): Option[BEncodedItem] = value.get(key)
  override def serialize = {
    val encodedItems = value.flatMap((p) => BEncodedString.fromString(p._1).serialize ++ p._2.serialize)
    ByteString("d") ++ encodedItems ++ ByteString("e")
  }
  override def +[B1 >: BEncodedItem](kv: (String, B1)): BEncodedMap = {
    new BEncodedMap(value + (kv._1 -> kv._2.asInstanceOf[BEncodedItem]))
  }
  override def -(k: String): BEncodedMap = new BEncodedMap(value - k)
  override def iterator = value.iterator
}