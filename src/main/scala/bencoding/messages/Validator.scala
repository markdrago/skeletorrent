package bencoding.messages

import bencoding.items.BEncodedMap

trait Validator {
  protected def checkRequiredElement(dict: BEncodedMap, key: String, dictDesc: String, cls: Class[_]) {
    require(dict.get(key).isDefined, s"Required '$key' element not present in $dictDesc")
    dict.get(key).get.getClass match {
      case `cls` => ()
      case _ => throw new IllegalArgumentException(s"'$key' in $dictDesc must be of type $cls")
    }
  }
}
