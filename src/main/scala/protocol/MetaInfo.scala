package protocol

import akka.util.ByteString
import java.security.MessageDigest
import bencoding._

class MetaInfo(val dict: BEncodedMap) {
  MetaInfoValidator.validate(dict)

  private def infoMap = dict.get("info").get

  def trackerUrl = dict.get("announce").get.toString

  def name = infoMap.get("name").get.toString

  def pieceLength = infoMap.get("piece length").get.toInt

  def isMultifile = infoMap.get("length").isEmpty

  def length:Option[Int] = infoMap.get("length").map(_.toInt)

  def pieces: List[ByteString] = {
    val bytes = infoMap.get("pieces").get match {
      case s:BEncodedString => s.value
      case _ => throw new IllegalStateException("metainfo.info.pieces must be a BEncodedString")
    }
    bytes.grouped(20).toList
  }

  def files: Option[List[MetaInfoFile]] = {
    infoMap.get("files").map((files) =>
      files.asInstanceOf[BEncodedList].map((file: BEncodedItem) => {
        val dict = file.asInstanceOf[BEncodedMap]
        new MetaInfoFile(dict.get("path").get.asInstanceOf[BEncodedList], dict.get("length").get.toInt)
      }).toList
    )
  }

  def infoHash: ByteString = {
    ByteString(MessageDigest.getInstance("SHA-1").digest(infoMap.serialize.toArray))
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
