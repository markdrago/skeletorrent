package bencoding.items

import akka.util.ByteString

case class BEncodedString(value: ByteString) extends BEncodedItem {

  override def encodedLength: Int = value.length + value.length.toString.length + 1

  override def toString = new String(value.toArray, "UTF-8")

  override def serialize = ByteString(value.length.toString + ":") ++ value
}

object BEncodedString {
  def fromString(str: String) = new BEncodedString(ByteString(str))
}