package bencoding

import akka.util.ByteString
import items._
import scala.collection.immutable.ListMap

class BEncoder {
  def encodeList(list: Seq[Any]): BEncodedItem = {
    new BEncodedList(list.map(encodeItem).toList)
  }

  def encodeMap(m: Map[String, _]): BEncodedMap = {
    var listMap = new ListMap[String, BEncodedItem]()

    m.foreach(
      (p: (String, Any)) => {
        listMap = listMap + ((p._1, encodeItem(p._2)))
      }
    )
    new BEncodedMap(listMap)
  }

  def encodeItem(i: Any): BEncodedItem = {
    i match {
      case b:BEncodedItem => b
      case b:ByteString => new BEncodedString(b)
      case s:String => BEncodedString.fromString(s)
      case i:Int => new BEncodedInt(i)
      case m:Map[_, _] => encodeMap(m.asInstanceOf[Map[String, Any]])
      case l:Seq[_] => encodeList(l)
      case _ => throw new IllegalArgumentException("Unexpected element type being BEncoded: " + i.getClass)
    }
  }
}
