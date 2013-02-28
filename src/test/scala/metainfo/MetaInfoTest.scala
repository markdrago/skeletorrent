package metainfo

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import javax.xml.bind.DatatypeConverter
import main.scala._
import akka.util.ByteString
import org.apache.commons.codec.binary.Base64
import bencoding.{BEncoder, BEncodedList, BDecoder}
import metainfo.{MetaInfoFile, MetaInfo}

class MetaInfoTest extends FunSuite with ShouldMatchers {
  test("MetaInfo contstructor will not allow creation of invalid MetaInfo file") {
    val dict = (new BDecoder).decodeMap(ByteString("d1:a1:be"))
    evaluating { new MetaInfo(dict) } should produce [IllegalArgumentException]
  }

  test("MetaInfo.apply will not allow creation of invalid MetaInfo file") {
    evaluating { MetaInfo(ByteString("d1:a1:be")) } should produce [IllegalArgumentException]
  }

  test("MetaInfo.apply will not allow creation of MetaInfo file with non-map type") {
    val caught = evaluating { MetaInfo(ByteString("1:a")) } should produce [IllegalArgumentException]
    caught.getMessage should include ("top-level Map")
  }

  test("name returns name") {
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).name should be ("freeculture.zip")
  }

  test("trackerUrl returns trackerUrl") {
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).trackerUrl should be ("http://www.legaltorrents.com:7070/announce")
  }

  test("pieceLength returns piece length") {
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).pieceLength should be (262144)
  }

  test("length returns length for single file metainfo") {
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).length.get should be (2133210)
  }

  test("length returns None for multi file metainfo") {
    MetaInfo(MetaInfoTest.get_metainfo_file_contents_multifile).length should be (None)
  }

  test("pieces returns pieces as a list of byte sequences") {
    val expected = MetaInfoTest.get_individual_piece_sha1sums
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).pieces should be (expected)
  }

  test("isMultifile returns false for single file metainfo") {
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).isMultifile should be (false)
  }

  test("isMultifile returns true for multiple file metainfo") {
    MetaInfo(MetaInfoTest.get_metainfo_file_contents_multifile).isMultifile should be (true)
  }

  test("files returns None for single file metainfo") {
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).files should be (None)
  }

  test("files returns list of MetaInfoFile objects for multi file metainfo") {
    val files = MetaInfo(MetaInfoTest.get_metainfo_file_contents_multifile).files.get
    files.length should be (2)
    files(0).path should be ("111.txt")
    files(0).length should be (111)
    files(1).path should be ("222.txt")
    files(1).length should be (222)
  }

  test("MetaInfoFile can correctly combine a list of path components in to a single path") {
    val pathList = (new BEncoder).encodeList(List("a", "b")).asInstanceOf[BEncodedList]
    val file = new MetaInfoFile(pathList, 123)
    file.path should be ("a/b")
  }

  test("infoHash can be calculated") {
    val expected = ByteString(DatatypeConverter.parseBase64Binary("ymrEu9lx05ApNdvPwtPqJbQopUc="))
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).infoHash should be (expected)
  }

  test("encodedInfoHash can be calculated") {
    val expected = "%CAj%C4%BB%D9q%D3%90%295%DB%CF%C2%D3%EA%25%B4%28%A5G"
    MetaInfo(MetaInfoTest.get_metainfo_file_contents).encodedInfoHash should be (expected)
  }
}

object MetaInfoTest {
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
    ByteString(DatatypeConverter.parseBase64Binary(encoded))
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
    ByteString(DatatypeConverter.parseBase64Binary(encoded))
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
    encoded.map(DatatypeConverter.parseBase64Binary(_)).map(ByteString(_))
  }
}
