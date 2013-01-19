package main.scala

abstract class BEncodedItem {
  def encodedLength: Int
  def value: Any

  override def equals(other: Any) : Boolean = {
    other.isInstanceOf[BEncodedItem]
    val otherItem = other.asInstanceOf[BEncodedItem]
    value.equals(otherItem.value)
  }

  override def hashCode = value.hashCode
}

class BEncodedString(val value: String) extends BEncodedItem {
  def encodedLength: Int = {
    return value.length + value.length.toString.length + 1
  }
}
object BEncodedString {
  implicit def bencodedString_to_String(enc: BEncodedString): String = enc.value
}

class BEncodedInt(val value: Int) extends BEncodedItem {
  def encodedLength: Int = {
    return value.toString.length + 2
  }
}
object BEncodedInt {
  implicit def bencodedInt_to_Int(enc: BEncodedInt): Int = enc.value
}

class BEncodedList(val value: List[BEncodedItem]) extends BEncodedItem {
  def encodedLength: Int = {
    return (2 /: value.map {e=>e.encodedLength}) {_+_}
  }
}
object BEncodedList {
  implicit def bencodedList_to_List(enc: BEncodedList): List[BEncodedItem] = enc.value
}

class BEncodedMap(val value: Map[BEncodedItem, BEncodedItem]) extends BEncodedItem {
  def encodedLength: Int = {
    return (2 /: value) ((acc, kv) => acc + kv._1.encodedLength + kv._2.encodedLength)
  }
}
object BEncodedMap {
  implicit def bencodedMap_to_Map(enc: BEncodedMap): Map[BEncodedItem, BEncodedItem] = enc.value
}
