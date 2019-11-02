package bencoding.items

import akka.util.ByteString
import scala.collection.immutable.ListMap

case class BEncodedMap(value: ListMap[String, BEncodedItem]) extends BEncodedItem with Map[String, BEncodedItem] {

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