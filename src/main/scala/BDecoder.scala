package main.scala

class BDecoder {
  /**
   * Decode a bencoded string
   * @param encoded a bencoded string
   * @return a new BEncodedString instance holding the now decoded string
   */
  def decodeString(encoded: String): BEncodedString = {
    require(encoded.matches("^\\d+:.*"), "bencoded string does not match expected format")
    val semi = encoded.indexOf(":")
    val len = Integer.valueOf(encoded.substring(0, semi))

    require(encoded.length() >= semi + len + 1, "bencoded string is shorter than expected")
    return new BEncodedString(encoded.substring(semi + 1, semi + len + 1))
  }

  /**
   * Decode a bencoded integer
   * @param encoded a bencoded integer
   * @return a new BEncodedInt instance holding the now decoded integer
   */
  def decodeInteger(encoded: String): BEncodedInt = {
    require(encoded.matches("^i\\-?\\d+e.*"), "bencoded integer does not match expected format")
    val end = encoded.indexOf("e")
    val intComponent = encoded.substring(1, end)

    require(! intComponent.equals("-0"), "bencoded integer of negative zero is invalid")
    require(!intComponent.charAt(0).equals('0') ||
            intComponent.equals("0"),
            "bencoded integer with leading zero is invalid")
    return new BEncodedInt(Integer.valueOf(intComponent))
  }

  /**
   * Decode a bencoded list
   * @param encoded a bencoded list
   * @return a new BEncodedList instance holding a List[BEncodedItem]
   */
  def decodeList(encoded: String): BEncodedList = {
    require(encoded.matches("^l.*"))
    var accumulator:List[BEncodedItem] = Nil

    var todecode = encoded.drop(1)
    while (! todecode.charAt(0).equals('e')) {
      val item = decodeItem(todecode)
      accumulator = item :: accumulator
      todecode = todecode.drop(item.encodedLength)
    }

    return new BEncodedList(accumulator.reverse)
  }

  /**
   * Decode a bencoded dictionary/map
   * @param encoded a bencoded dictionary
   * @return a new BEncodedMap instance holding a Map[BEncodedItem, BEncodedItem]
   */
  def decodeMap(encoded: String): BEncodedMap = {
    require(encoded.matches("^d.*"))
    var accumulator:Map[BEncodedItem, BEncodedItem] = Map.empty

    var todecode = encoded.drop(1)
    while (! todecode.charAt(0).equals('e')) {
      val key = decodeItem(todecode)
      todecode = todecode.drop(key.encodedLength)
      val value = decodeItem(todecode)
      todecode = todecode.drop(value.encodedLength)
      accumulator = accumulator + (key -> value)
    }

    return new BEncodedMap(accumulator)
  }

  /**
   * Decode a bencoded item
   * @param encoded a data structure which has been bencoded
   * @return a tuple containing a new BEncodedItem and the remainder of the original encoded string
   */
  def decodeItem(encoded: String): BEncodedItem = {
    return encoded.charAt(0) match {
      case 'i' => decodeInteger(encoded)
      case 'l' => decodeList(encoded)
      case 'd' => decodeMap(encoded)
      case n if '0' to '9' contains n => decodeString(encoded)
      case _ => throw new IllegalArgumentException("unidentifed format for bencoded item, expecting first character of [ild0-9], found: " + encoded.charAt(0))
    }
  }
}
