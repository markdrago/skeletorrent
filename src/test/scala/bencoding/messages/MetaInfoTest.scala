package bencoding.messages

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import javax.xml.bind.DatatypeConverter
import akka.util.ByteString
import bencoding.{BEncoder, BDecoder}
import bencoding.items.BEncodedList

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
    MetaInfo(MetaInfoSample.get_metainfo_file_contents).name should be ("freeculture.zip")
  }

  test("trackerUrl returns trackerUrl") {
    MetaInfo(MetaInfoSample.get_metainfo_file_contents).trackerUrl should be ("http://www.legaltorrents.com:7070/announce")
  }

  test("pieceLength returns piece length") {
    MetaInfo(MetaInfoSample.get_metainfo_file_contents).pieceLength should be (262144)
  }

  test("length returns length for single file metainfo") {
    MetaInfo(MetaInfoSample.get_metainfo_file_contents).length.get should be (2133210)
  }

  test("length returns None for multi file metainfo") {
    MetaInfo(MetaInfoSample.get_metainfo_file_contents_multifile).length should be (None)
  }

  test("pieces returns pieces as a list of byte sequences") {
    val expected = MetaInfoSample.get_individual_piece_sha1sums
    MetaInfo(MetaInfoSample.get_metainfo_file_contents).pieces should be (expected)
  }

  test("isMultifile returns false for single file metainfo") {
    MetaInfo(MetaInfoSample.get_metainfo_file_contents) should not be a ('multifile)
  }

  test("isMultifile returns true for multiple file metainfo") {
    MetaInfo(MetaInfoSample.get_metainfo_file_contents_multifile) should be a ('multifile)
  }

  test("files returns None for single file metainfo") {
    MetaInfo(MetaInfoSample.get_metainfo_file_contents).files should be (None)
  }

  test("files returns list of MetaInfoFile objects for multi file metainfo") {
    val files = MetaInfo(MetaInfoSample.get_metainfo_file_contents_multifile).files.get
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
    MetaInfo(MetaInfoSample.get_metainfo_file_contents).infoHash should be (expected)
  }
}