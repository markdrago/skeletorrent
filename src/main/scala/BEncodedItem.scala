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

class BEncodedInt(val value: Int) extends BEncodedItem {
  def encodedLength: Int = {
    return value.toString.length + 2
  }
}

class BEncodedList(val value: List[BEncodedItem]) extends BEncodedItem {
  def encodedLength: Int = {
    return (2 /: value.map {e=>e.encodedLength}) {_+_}
  }
}

class BEncodedMap(val value: Map[BEncodedItem, BEncodedItem]) extends BEncodedItem {
  def encodedLength: Int = {
    return (2 /: value) ((acc, kv) => acc + kv._1.encodedLength + kv._2.encodedLength)
  }
}
