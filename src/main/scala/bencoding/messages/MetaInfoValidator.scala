package bencoding.messages

import bencoding._
import items._
import items.BEncodedInt
import items.BEncodedList

object MetaInfoValidator extends Validator {
  def validate(dict: BEncodedMap) {
    //validate attributes required by all (supported) metainfo files
    checkRequiredElement(dict, "announce", "MetaInfo", classOf[BEncodedString])
    checkRequiredElement(dict, "info", "Metainfo", classOf[BEncodedMap])

    val infoMap = dict.get("info").get.asInstanceOf[BEncodedMap]
    checkRequiredElement(infoMap, "name", "Metainfo/info", classOf[BEncodedString])
    checkRequiredElement(infoMap, "piece length", "Metainfo/info", classOf[BEncodedInt])
    checkRequiredElement(infoMap, "pieces", "Metainfo/info", classOf[BEncodedString])

    //validity check is different for single/multi file torrents
    if (infoMap.get("length").isEmpty && infoMap.get("files").isDefined) {
      validateMultiFileStructure(dict)
    } else if (infoMap.get("length").isDefined && infoMap.get("files").isEmpty) {
      validateSingleFileStructure(dict)
    } else {
      throw new IllegalArgumentException("One (and not both) of length & files elements must be present in Metainfo/info")
    }
  }

  private def validateSingleFileStructure(dict: BEncodedMap) {
    val infoMap = dict.get("info").get.asInstanceOf[BEncodedMap]
    checkForbiddenElement(infoMap, "files", "Metainfo/info")
    checkRequiredElement(infoMap, "length", "Metainfo/info", classOf[BEncodedInt])
  }

  private def validateMultiFileStructure(dict: BEncodedMap) {
    val infoMap = dict.get("info").get.asInstanceOf[BEncodedMap]
    checkForbiddenElement(infoMap, "length", "Metainfo/info")

    checkRequiredElement(infoMap, "files", "Metainfo/info", classOf[BEncodedList])
    val filesList = infoMap.get("files").get.asInstanceOf[BEncodedList]
    filesList.map(validateMultiFileFileElement)
  }

  private def validateMultiFileFileElement(item: BEncodedItem) {
    val dict = item match {
      case m:BEncodedMap => m
      case _ => throw new IllegalArgumentException("Multifile Metainfo files[X] was not a dictionary")
    }

    checkRequiredElement(dict, "length", "Metainfo/info/files[X]", classOf[BEncodedInt])
    checkRequiredElement(dict, "path", "Metainfo/info/files[X]", classOf[BEncodedList])

    val path = dict.get("path").get.asInstanceOf[BEncodedList]
    validateMultiFileFilePathElement(path)
  }

  private def validateMultiFileFilePathElement(list: BEncodedList) {
    require(list.length > 0, "Multifile Metainfo path was an empty list")

    list.map((i: BEncodedItem) => {
      i match {
        case s:BEncodedString => s
        case _ => throw new IllegalArgumentException("Multifile Metainfo path list contained a non-string")
      }
    })
  }

  private def checkForbiddenElement(dict: BEncodedMap, key: String, dictDesc: String) {
    require(dict.get(key).isEmpty, s"'$key' must not be present in $dictDesc")
  }
}
