package bencoding

import akka.util.ByteString

class BDecoder {
  /**
   * Decode a bencoded string or byte sequence
   * @param encoded a bencoded ByteString
   * @return a new BEncodedString instance holding the now decoded string / byte sequence
   */
  def decodeString(encoded: ByteString): BEncodedString = {
    checkStringFormat(encoded)
    val digits = encoded.takeWhile(byteIsDigit(_))
    val len = Integer.valueOf(new String(digits.toArray, "UTF-8"))
    require(encoded.length >= digits.length + len + 1, "bencoded string is shorter than expected")
    new BEncodedString(encoded.slice(digits.length + 1, digits.length + len + 1))
  }

  def checkStringFormat(bytes: ByteString) = {
    //must start with a bunch of digits then a :
    require(bytes.length > 0, "bencoded string must not be of length 0")
    require(byteIsDigit(bytes(0)), "bencoded string must start with a digit")
    require(bytes.dropWhile(byteIsDigit)(0) == ':'.toByte, "bencoded string must begin with digits followed by a :")
  }

  def byteIsDigit(b: Byte): Boolean = {
    b >= '0'.toByte && b <= '9'.toByte
  }

  /**
   * Decode a bencoded integer
   * @param encoded a bencoded integer
   * @return a new BEncodedInt instance holding the now decoded integer
   */
  def decodeInteger(encoded: ByteString): BEncodedInt = {
    require(encoded.length >= 3, "bencoded integer must be at least 3 bytes long")
    require(encoded(0) == 'i'.toByte, "bencoded integer must start with an i")

    val intComponent = new String(encoded.drop(1).takeWhile(_ != 'e'.toByte).toArray)
    require(encoded.length > intComponent.length + 1, "bencoded integer is too short to have trailing 'e'")
    require(intComponent.matches("^\\-?[1-9][0-9]*$") || intComponent == "0",
      "bencoded integer does not match expected format")

    new BEncodedInt(Integer.valueOf(intComponent))
  }

  /**
   * Decode a bencoded list
   * @param encoded a bencoded list
   * @return a new BEncodedList instance holding a List[BEncodedItem]
   */
  def decodeList(encoded: ByteString): BEncodedList = {
    require(encoded(0) == 'l'.toByte)
    var accumulator:List[BEncodedItem] = Nil

    var todecode = encoded.drop(1)
    while (todecode(0) != 'e'.toByte) {
      val item = decodeItem(todecode)
      accumulator = item :: accumulator
      todecode = todecode.drop(item.encodedLength)
    }

    new BEncodedList(accumulator.reverse)
  }

  /**
   * Decode a bencoded dictionary/map
   * @param encoded a bencoded dictionary
   * @return a new BEncodedMap instance holding a Map[String, BEncodedItem]
   */
  def decodeMap(encoded: ByteString): BEncodedMap = {
    require(encoded(0) == 'd'.toByte)
    var accumulator:Map[String, BEncodedItem] = Map.empty

    var todecode = encoded.drop(1)
    while (todecode(0) != 'e'.toByte) {
      val key = decodeItem(todecode)
      todecode = todecode.drop(key.encodedLength)

      key match {
        case k:BEncodedString => ()
        case _ => throw new IllegalArgumentException("BEncodedMaps must have strings for keys")
      }

      val value = decodeItem(todecode)
      todecode = todecode.drop(value.encodedLength)
      accumulator = accumulator + (key.toString -> value)
    }

    new BEncodedMap(accumulator)
  }

  /**
   * Decode a bencoded item
   * @param encoded a data structure which has been bencoded
   * @return a new BEncodedItem
   */
  def decodeItem(encoded: ByteString): BEncodedItem = {
    return encoded(0).toChar match {
      case 'i' => decodeInteger(encoded)
      case 'l' => decodeList(encoded)
      case 'd' => decodeMap(encoded)
      case n if '0' to '9' contains n => decodeString(encoded)
      case _ => throw new IllegalArgumentException("unidentifed format for bencoded item, expecting first character of [ild0-9], found: " + encoded(0))
    }
  }

  /* Overload decodeItem to accept a string and assume it is UTF-8 */
  def decodeItem(encoded: String): BEncodedItem = {
    decodeItem(ByteString(encoded, "UTF-8"))
  }
}
