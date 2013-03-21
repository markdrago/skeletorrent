package bencoding.messages

import akka.util.ByteString
import javax.xml.bind.DatatypeConverter

object TrackerResponseSample {
  def valid_response: Map[String, Any] = {
    Map(
      "interval" -> 800,
      "peers" -> List(
        Map(
          "peer id" -> "abcdefghijklmnopqrst",
          "ip" -> "1.2.3.4",
          "port" -> 6881),
        Map(
          "peer id" -> "12345678901234567890",
          "ip" -> "130.130.130.130",
          "port" -> 51415
        )
      )
    )
  }

  /* contents:
  d
  8:completei24e
  10:incompletei1e
  8:intervali1800e
  5:peers
  l
  d 2:ip 14:201.82.132.144 7:peer id 20:-UT2210-(12 bytes)   4:port i47239e e
  d 2:ip 11:8.19.35.234    7:peer id 20:-lt0C20-(12 bytes)   4:port i9898e  e
  d 2:ip 13:78.46.105.140  7:peer id 20:-TR2030-1hdkfao587yq 4:port i51415e e
  d 2:ip 12:71.122.11.61   7:peer id 20:-DE1350-X~)Th(_O8_it 4:port i51413e e
  d 2:ip 13:91.237.197.22  7:peer id 20:-TR2420-u2hdnpsruwdc 4:port i51413e e
  e
  e
  */
  def valid_real_world_response = {
    val encoded =
      "ZDg6Y29tcGxldGVpMjRlMTA6aW5jb21wbGV0ZWkxZTg6aW50ZXJ2YWxpMTgwMGU1OnBlZXJzbGQy" +
        "OmlwMTQ6MjAxLjgyLjEzMi4xNDQ3OnBlZXIgaWQyMDotVVQyMjEwLSpiW4zdohkcv/2FaTQ6cG9y" +
        "dGk0NzIzOWVlZDI6aXAxMTo4LjE5LjM1LjIzNDc6cGVlciBpZDIwOi1sdDBDMjAtJqCLEzCGmVTd" +
        "1LHgNDpwb3J0aTk4OThlZWQyOmlwMTM6NzguNDYuMTA1LjE0MDc6cGVlciBpZDIwOi1UUjIwMzAt" +
        "MWhka2ZhbzU4N3lxNDpwb3J0aTUxNDE1ZWVkMjppcDEyOjcxLjEyMi4xMS42MTc6cGVlciBpZDIw" +
        "Oi1ERTEzNTAtWH4pVGgoX084X2l0NDpwb3J0aTUxNDEzZWVkMjppcDEzOjkxLjIzNy4xOTcuMjI3" +
        "OnBlZXIgaWQyMDotVFIyNDIwLXUyaGRucHNydXdkYzQ6cG9ydGk1MTQxM2VlZWU="
    ByteString(DatatypeConverter.parseBase64Binary(encoded))
  }
}