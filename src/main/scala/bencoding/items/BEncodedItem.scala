package bencoding.items

import akka.util.ByteString
import scala.language.implicitConversions

/* inspired by the JsValue JSON stuff in the play framework */

trait BEncodedItem {

  def encodedLength: Int

  def serialize: ByteString

  def get(key: String): Option[BEncodedItem] = {
    throw new IllegalStateException("BEncodedItem is not a map: " + this.toString)
  }

  def toInt: Int = {
    throw new IllegalStateException("BEncodedItem is not an integer: " + this.toString)
  }
}