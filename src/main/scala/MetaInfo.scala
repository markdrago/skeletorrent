package main.scala

class MetaInfo(val dict: BEncodedMap) {

}

object MetaInfo {
  val bdecoder = new BDecoder

  def apply(bytes: Seq[Byte]): MetaInfo = {
    val bencodedItem = bdecoder.decodeItem(bytes)

    bencodedItem match {
      case m:BEncodedMap => return new MetaInfo(m)
      case _ => throw new IllegalArgumentException("MetaInfo file must contain a top-level Map")
    }
  }
}