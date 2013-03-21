package bencoding.items

import akka.util.ByteString

case class BEncodedList(value: List[BEncodedItem] = List()) extends BEncodedItem with Traversable[BEncodedItem] {

  def length = value.length

  override def encodedLength: Int = (2 /: value.map {e=>e.encodedLength}) {_+_}

  def apply(index: Int): BEncodedItem = value(index)

  override def serialize = ByteString("l") ++ value.flatMap(_.serialize) ++ ByteString("e")

  override def foreach[A](f: BEncodedItem => A) = value.foreach(f)
}
