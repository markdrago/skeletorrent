package bencoding.items

import akka.util.ByteString

case class BEncodedInt(value: Int) extends BEncodedItem {

  override def encodedLength: Int = value.toString.length + 2

  override def toInt: Int = value

  override def serialize = ByteString("i" + value.toString + "e")
}
