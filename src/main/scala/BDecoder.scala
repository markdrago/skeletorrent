package main.scala

class BDecoder {
  /**
   * Decode a bencoded string or byte sequence
   * @param encoded a bencoded string
   * @return a new BEncodedString instance holding the now decoded string / byte sequence
   */
  def decodeString(encoded: Seq[Byte]): BEncodedString = {
    checkStringFormat(encoded)
    val digits = encoded.takeWhile(byteIsDigit(_)).toArray
    val len = Integer.valueOf(new String(digits))
    require(encoded.length >= digits.length + len + 1, "bencoded string is shorter than expected")
    new BEncodedString(encoded.slice(digits.length + 1, digits.length + len + 1))
  }

  def checkStringFormat(bytes: Seq[Byte]) = {
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
  def decodeInteger(encoded: Seq[Byte]): BEncodedInt = {
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
  def decodeList(encoded: Seq[Byte]): BEncodedList = {
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
   * @return a new BEncodedMap instance holding a Map[BEncodedItem, BEncodedItem]
   */
  def decodeMap(encoded: Seq[Byte]): BEncodedMap = {
    require(encoded(0) == 'd'.toByte)
    var accumulator:Map[BEncodedItem, BEncodedItem] = Map.empty

    var todecode = encoded.drop(1)
    while (todecode(0) != 'e'.toByte) {
      val key = decodeItem(todecode)
      todecode = todecode.drop(key.encodedLength)
      val value = decodeItem(todecode)
      todecode = todecode.drop(value.encodedLength)
      accumulator = accumulator + (key -> value)
    }

    new BEncodedMap(accumulator)
  }

  /**
   * Decode a bencoded item
   * @param encoded a data structure which has been bencoded
   * @return a tuple containing a new BEncodedItem and the remainder of the original encoded string
   */
  def decodeItem(encoded: Seq[Byte]): BEncodedItem = {
    return encoded(0).toChar match {
      case 'i' => decodeInteger(encoded)
      case 'l' => decodeList(encoded)
      case 'd' => decodeMap(encoded)
      case n if '0' to '9' contains n => decodeString(encoded)
      case _ => throw new IllegalArgumentException("unidentifed format for bencoded item, expecting first character of [ild0-9], found: " + encoded(0))
    }
  }
}
