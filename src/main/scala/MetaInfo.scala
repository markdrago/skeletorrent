package main.scala

class MetaInfo(val dict: BEncodedMap) {
  MetaInfo.checkMetaInfoValidity(dict)

  private def infoMap = dict.get("info").get

  def trackerUrl = dict.get("announce").get.toString
  def fileName = infoMap.get("name").get.toString
  def pieceLength = infoMap.get("piece length").get.toInt
  def length = infoMap.get("length").get.toInt
  def pieces: List[Seq[Byte]] = {
    val bytes = infoMap.get("pieces").get match {
      case s:BEncodedString => s.value
      case _ => throw new IllegalStateException("metainfo.info.pieces must be a BEncodedString")
    }
    bytes.grouped(20).toList
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

  def checkMetaInfoValidity(dict: BEncodedMap) = {
    def checkRequiredElement(dict: BEncodedMap, key: String, dictDesc: String, cls: Class[_]) = {
      if (dict.get(key).isEmpty) throw new IllegalArgumentException(s"Required '$key' element not present in $dictDesc")
      dict.get(key).get.getClass match {
        case `cls` => ()
        case _ => throw new IllegalArgumentException(s"'$key' in $dictDesc must be of type $cls")
      }
    }

    checkRequiredElement(dict, "announce", "MetaInfo", classOf[BEncodedString])
    checkRequiredElement(dict, "info", "Metainfo", classOf[BEncodedMap])

    val infoMap = dict.get("info").get.asInstanceOf[BEncodedMap]
    checkRequiredElement(infoMap, "name", "Metainfo/info", classOf[BEncodedString])
    checkRequiredElement(infoMap, "piece length", "Metainfo/info", classOf[BEncodedInt])
    checkRequiredElement(infoMap, "length", "Metainfo/info", classOf[BEncodedInt])
    checkRequiredElement(infoMap, "pieces", "Metainfo/info", classOf[BEncodedString])
  }
}