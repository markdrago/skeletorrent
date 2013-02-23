package main.scala

object MetaInfoValidator {
  def validate(dict: BEncodedMap) {
    //validate attributes required by all (supported) metainfo files
    checkRequiredElement(dict, "announce", "MetaInfo", classOf[BEncodedString])
    val infoMap = checkRequiredElement(dict, "info", "Metainfo", classOf[BEncodedMap])

    checkRequiredElement(infoMap, "name", "Metainfo/info", classOf[BEncodedString])
    checkRequiredElement(infoMap, "piece length", "Metainfo/info", classOf[BEncodedInt])
    checkRequiredElement(infoMap, "pieces", "Metainfo/info", classOf[BEncodedString])

    //validity check is different for single/multi file torrents
    if (infoMap.get("length").isEmpty && infoMap.get("files").isDefined) {
      validateMultiFileStructure(infoMap)
    } else if (infoMap.get("length").isDefined && infoMap.get("files").isEmpty) {
      validateSingleFileStructure(infoMap)
    } else {
      throw new IllegalArgumentException("One (and not both) of length & files elements must be present in Metainfo/info")
    }
  }

  private def validateSingleFileStructure(infoMap: BEncodedMap) {
    checkForbiddenElement(infoMap, "files", "Metainfo/info")
    checkRequiredElement(infoMap, "length", "Metainfo/info", classOf[BEncodedInt])
  }

  private def validateMultiFileStructure(infoMap: BEncodedMap) {
    checkForbiddenElement(infoMap, "length", "Metainfo/info")

    val filesList = checkRequiredElement(infoMap, "files", "Metainfo/info", classOf[BEncodedList])
    filesList.map(validateMultiFileFileElement)
  }

  private def validateMultiFileFileElement(item: BEncodedItem) {
    val dict = item match {
      case m:BEncodedMap => m
      case _ => throw new IllegalArgumentException("Multifile Metainfo files[X] was not a dictionary")
    }

    checkRequiredElement(dict, "length", "Metainfo/info/files[X]", classOf[BEncodedInt])
    val path = checkRequiredElement(dict, "path", "Metainfo/info/files[X]", classOf[BEncodedList])

    validateMultiFileFilePathElement(path)
  }

  private def validateMultiFileFilePathElement(list: BEncodedList) {
    if (list.length == 0) {
      throw new IllegalArgumentException("Multifile Metainfo path was an empty list")
    }

    list.map((i: BEncodedItem) => {
      i match {
        case s:BEncodedString => s
        case _ => throw new IllegalArgumentException("Multifile Metainfo path list contained a non-string")
      }
    })
  }

  private def checkRequiredElement[A](dict: BEncodedMap, key: String, dictDesc: String, cls: Class[A]): A = {
    if (dict.get(key).isEmpty) throw new IllegalArgumentException(s"Required '$key' element not present in $dictDesc")
    dict.get(key).get.getClass match {
      case `cls` => _
      case _ => throw new IllegalArgumentException(s"'$key' in $dictDesc must be of type $cls")
    }
  }

  private def checkForbiddenElement(dict: BEncodedMap, key: String, dictDesc: String) {
    if (dict.get(key).isDefined) {
      throw new IllegalArgumentException(s"'$key' must not be present in $dictDesc")
    }
  }
}
