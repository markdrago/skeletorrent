package main.scala

class MetaInfo(val dict: BEncodedMap) {
  def trackerUrl: Option[String] = {
    if (dict.get("announce").isEmpty) return None
    return new Some[String](dict.get("announce").get.toString)
  }

}

object MetaInfo {
  val bdecoder = new BDecoder

  def apply(bytes: Seq[Byte]): MetaInfo = {
    val bencodedItem = bdecoder.decodeItem(bytes)

    bencodedItem match {
      case m:BEncodedMap => return new MetaInfo(m)
      case _ => throw new IllegalArgumentException("MetaInfo data must contain a top-level Map")
    }
  }
}