package bencoding.messages

import akka.util.ByteString
import java.util.Base64

object MetaInfoSample {
  val base64Decoder = Base64.getDecoder()

  def get_metainfo_map_for_single_file: Map[String, Any] = {
    Map(
      "announce" -> "http://www.legaltorrents.com:7070/announce",
      "creation date" -> 1081312084,
      "info" -> Map(
        "length" -> 2133210,
        "name" -> "freeculture.zip",
        "piece length" -> 262144,
        "pieces" -> get_pieces_sha1sums
      )
    )
  }

  def get_metainfo_map_for_multi_file: Map[String, Any] = {
    Map(
      "announce" -> "http://www.legaltorrents.com:7070/announce",
      "creation date" -> 1081312084,
      "info" -> Map(
        "name" -> "freeculture",
        "piece length" -> 262144,
        "files" -> List(
          Map("path" -> List("111.txt"), "length" -> 111),
          Map("path" -> List("222.txt"), "length" -> 222)
        ),
        "pieces" -> get_pieces_sha1sums
      )
    )
  }

  def get_metainfo_file_contents: ByteString = {
    /* contents:
     * d8:announce42:http://www.legaltorrents.com:7070/announce13:creation datei1081312084e
     * 4:infod6:lengthi2133210e4:name15:freeculture.zip12:piece lengthi262144e6:pieces180:
     * <binary sha1sums>
     * ee
     */
    val encoded =
      "ZDg6YW5ub3VuY2U0MjpodHRwOi8vd3d3LmxlZ2FsdG9ycmVudHMuY29tOjcwNzAvYW5ub3VuY2Ux" +
      "MzpjcmVhdGlvbiBkYXRlaTEwODEzMTIwODRlNDppbmZvZDY6bGVuZ3RoaTIxMzMyMTBlNDpuYW1l" +
      "MTU6ZnJlZWN1bHR1cmUuemlwMTI6cGllY2UgbGVuZ3RoaTI2MjE0NGU2OnBpZWNlczE4MDrtiec4" +
      "Sw00D6X/+hnRoA2I51S+AFBcbtsV0weqjkY4kfTSUw6N0TkKBHbzlzuhabVhAx7MQyh1GkwAlOHy" +
      "MUlEH0osleKnyMGAS73zefOd3E5DVssVKnpLa8XMHJB5q9Hk98QiXmhc/GNWL4Gu1pEaD5LIMCbU" +
      "2Gc8oiM0J8BrIMLO0Ca3uEkys8EmVdvvSeAFc3OsidBWJ1jzkaX33qtSst1ZrxLjRBuLU5P/jXbe" +
      "a8GRD4RlZQ=="
    ByteString(base64Decoder.decode(encoded))
  }

  def get_metainfo_file_contents_multifile: ByteString = {
    /* contents:
     * d8:announce42:http://www.legaltorrents.com:7070/announce13:creation datei1081312084e
     * 4:infod4:name14:directory_name12:piece lengthi262144e
     * 5:filesld4:pathl7:111.txte6:lengthi111eed4:pathl7:222.txte6:lengthi222eee
     * 6:pieces180:<binary sha1sums>
     * ee
     */
    val encoded =
      "ZDg6YW5ub3VuY2U0MjpodHRwOi8vd3d3LmxlZ2FsdG9ycmVudHMuY29tOjcwNzAvYW5ub3VuY2Ux" +
      "MzpjcmVhdGlvbiBkYXRlaTEwODEzMTIwODRlNDppbmZvZDQ6bmFtZTE0OmRpcmVjdG9yeV9uYW1l" +
      "MTI6cGllY2UgbGVuZ3RoaTI2MjE0NGU1OmZpbGVzbGQ0OnBhdGhsNzoxMTEudHh0ZTY6bGVuZ3Ro" +
      "aTExMWVlZDQ6cGF0aGw3OjIyMi50eHRlNjpsZW5ndGhpMjIyZWVlNjpwaWVjZXMxODA67YnnOEsN" +
      "NA+l//oZ0aANiOdUvgBQXG7bFdMHqo5GOJH00lMOjdE5CgR285c7oWm1YQMezEModRpMAJTh8jFJ" +
      "RB9KLJXip8jBgEu983nzndxOQ1bLFSp6S2vFzByQeavR5PfEIl5oXPxjVi+BrtaRGg+SyDAm1Nhn" +
      "PKIjNCfAayDCztAmt7hJMrPBJlXb70ngBXNzrInQVidY85Gl996rUrLdWa8S40Qbi1OT/4123mvB" +
      "kQ+EZWU="
    ByteString(base64Decoder.decode(encoded))
  }

  def get_pieces_sha1sums: ByteString = get_individual_piece_sha1sums.reduceLeft(_++_)

  def get_individual_piece_sha1sums: List[ByteString] = {
    val encoded = List(
      "7YnnOEsNNA+l//oZ0aANiOdUvgA=",
      "UFxu2xXTB6qORjiR9NJTDo3ROQo=",
      "BHbzlzuhabVhAx7MQyh1GkwAlOE=",
      "8jFJRB9KLJXip8jBgEu983nzndw=",
      "TkNWyxUqektrxcwckHmr0eT3xCI=",
      "Xmhc/GNWL4Gu1pEaD5LIMCbU2Gc=",
      "PKIjNCfAayDCztAmt7hJMrPBJlU=",
      "2+9J4AVzc6yJ0FYnWPORpffeq1I=",
      "st1ZrxLjRBuLU5P/jXbea8GRD4Q="
    )
    encoded.map(base64Decoder.decode(_)).map(ByteString(_))
  }
}
