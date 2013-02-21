package main.scala

import akka.util.ByteString

class MetaInfo(val dict: BEncodedMap) {
  MetaInfoValidator.validate(dict)

  private def infoMap = dict.get("info").get

  def trackerUrl = dict.get("announce").get.toString
  def name = infoMap.get("name").get.toString
  def pieceLength = infoMap.get("piece length").get.toInt
  def isMultifile = infoMap.get("length").isEmpty
  def length:Option[Int] = {
    val length = infoMap.get("length")
    if (length.isDefined) { return Some(length.get.toInt)}
    return None
  }
  def pieces: List[ByteString] = {
    val bytes = infoMap.get("pieces").get match {
      case s:BEncodedString => s.value
      case _ => throw new IllegalStateException("metainfo.info.pieces must be a BEncodedString")
    }
    bytes.grouped(20).toList
  }
  def files: Option[List[MetaInfoFile]] = {
    val files = infoMap.get("files")
    if (files.isEmpty) { return None }
    Some(
      files.get.asInstanceOf[BEncodedList].map((file: BEncodedItem) => {
        val dict = file.asInstanceOf[BEncodedMap]
        new MetaInfoFile(dict.get("path").get.asInstanceOf[BEncodedList], dict.get("length").get.toInt)
      }).toList
    )
  }
}

class MetaInfoFile(val pathList: BEncodedList, val length: Int) {
  def path: String = pathList.map(_.toString).mkString("/")
}

object MetaInfo {
  val bdecoder = new BDecoder

  def apply(bytes: ByteString): MetaInfo = {
    val bencodedItem = bdecoder.decodeItem(bytes)

    bencodedItem match {
      case m:BEncodedMap => new MetaInfo(m)
      case _ => throw new IllegalArgumentException("MetaInfo data must contain a top-level Map")
    }
  }
}
