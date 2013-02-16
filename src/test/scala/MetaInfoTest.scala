package test.scala

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import javax.xml.bind.DatatypeConverter
import main.scala._

class MetaInfoTest extends FunSuite with ShouldMatchers {

  test("checkMetaInfoValidity returns error when announce element is not present") {
    checkMetaInfoValidityForFailure("d1:a1:be", "announce")
  }

  test("checkMetaInfoValidity returns error when info element is not present") {
    checkMetaInfoValidityForFailure("d8:announce1:be", "info")
  }

  test("checkMetaInfoValidity returns error when info element is not a map") {
    checkMetaInfoValidityForFailure("d8:announce1:b4:infoi2ee", "info")
  }

  test("checkMetaInfoValidity returns error when info/name element is not present") {
    checkMetaInfoValidityForFailure("d8:announce1:b4:infod1:a1:bee", "name")
  }

  test("checkMetaInfoValidity returns error when info/piece_length element is not present") {
    checkMetaInfoValidityForFailure("d8:announce1:b4:infod4:name1:bee", "piece length")
  }

  test("checkMetaInfoValidity returns error when info/length element is not present") {
    checkMetaInfoValidityForFailure("d8:announce1:b4:infod4:name1:b12:piece length1:cee", "length")
  }

  test("checkMetaInfoValidity returns error when info/pieces element is not present") {
    checkMetaInfoValidityForFailure("d8:announce1:b4:infod4:name1:b12:piece lengthi12e6:lengthi123eee", "pieces")
  }

  def checkMetaInfoValidityForFailure(input: String, expectedMissing: String) = {
    val dict = (new BDecoder).decodeMap(input.getBytes("UTF-8"))
    val caught = evaluating { MetaInfo.checkMetaInfoValidity(dict) } should produce [IllegalArgumentException]
    caught.getMessage should include (expectedMissing)
  }

  test("checkMetaInfoValidity does not throw exception for a valid MetaInfo file") {
    val dict = (new BDecoder).decodeMap(get_metainfo_file_contents)
    MetaInfo.checkMetaInfoValidity(dict)
  }

  test("MetaInfo contstructor will not allow creation of invalid MetaInfo file") {
    val dict = (new BDecoder).decodeMap("d1:a1:be".getBytes("UTF-8"))
    evaluating { new MetaInfo(dict) } should produce [IllegalArgumentException]
  }

  test("MetaInfo.apply will not allow creation of invalid MetaInfo file") {
    evaluating { MetaInfo("d1:a1:be".getBytes("UTF-8")) } should produce [IllegalArgumentException]
  }

  test("MetaInfo.apply will not allow creation of MetaInfo file with non-map type") {
    evaluating { MetaInfo("1:a".getBytes("UTF-8")) } should produce [IllegalArgumentException]
  }

  test("fileName returns fileName") {
    MetaInfo(get_metainfo_file_contents).fileName should be ("freeculture.zip")
  }

  test("trackerUrl returns trackerUrl") {
    MetaInfo(get_metainfo_file_contents).trackerUrl should be ("http://www.legaltorrents.com:7070/announce")
  }

  test("pieceLength returns piece length") {
    MetaInfo(get_metainfo_file_contents).pieceLength should be (262144)
  }

  test("length returns length") {
    MetaInfo(get_metainfo_file_contents).length should be (2133210)
  }

  test("pieces returns pieces as a list of byte sequences") {
    val expected = get_individual_piece_md5sums
    MetaInfo(get_metainfo_file_contents).pieces should be (expected)
  }

  //announce
  //info
    //name (single file is filename, multi-file is directory name)
    //piece length 1
    //pieces
    //length XOR files
      //length
      //files (list of dicts)
        //length, path (list of strings representing exploded path)

  def get_metainfo_file_contents: Seq[Byte] = {
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
    DatatypeConverter.parseBase64Binary(encoded)
  }

  def get_individual_piece_md5sums: List[Seq[Byte]] = {
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
    return encoded.map(DatatypeConverter.parseBase64Binary(_).toSeq)
  }
}
