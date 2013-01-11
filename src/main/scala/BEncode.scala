package main.scala

class BEncode {
  /**
   * Decode a bencoded string in to a plain string
   * @param encoded: String
   * @return (decoded string: String, length of encoded string: Int)
   */
  def decodeString(encoded: String): (String, Int) = {
    require(encoded.matches("^\\d+:.*"), "bencoded string does not match expected format")
    val semi = encoded.indexOf(":")
    val len = Integer.valueOf(encoded.substring(0, semi))

    require(encoded.length() >= semi + len + 1, "bencoded string is shorter than expected")
    return (
      encoded.substring(semi + 1, semi + len + 1),
      semi + len + 1
    )
  }

  /**
   * Decode a bencoded integer in to an Int
   * @param encoded: String
   * @return (decoded integer: Int, length of encoded string: Int)
   */
  def decodeInteger(encoded: String): (Int, Int) = {
    require(encoded.matches("^i\\-?\\d+e"), "bencoded integer does not match expected format")
    val end = encoded.indexOf("e")
    val intComponent = encoded.substring(1, end)

    require(! intComponent.equals("-0"), "bencoded integer of negative zero is invalid")
    require(!intComponent.charAt(0).equals('0') ||
            intComponent.equals("0"),
            "bencoded integer with leading zero is invalid")
    return (Integer.valueOf(intComponent), end + 1)
  }
}
