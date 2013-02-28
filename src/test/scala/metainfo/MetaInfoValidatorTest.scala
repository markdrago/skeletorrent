package metainfo

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import bencoding.{BEncoder, BDecoder}

class MetaInfoValidatorTest extends FunSuite with ShouldMatchers {

  //both single & multifile
  test("checkMetaInfoValidity throws when announce element is not present") {
    checkMetaInfoValidityForMissingTopLevelElement("announce")
  }

  test("checkMetaInfoValidity throws when announce is not a string") {
    val map = MetaInfoTest.get_metainfo_map_for_single_file - "announce" + ("announce" -> 10)
    checkMetaInfoValidityForFailure(map, "announce")
  }

  test("checkMetaInfoValidity throws when info element is not present") {
    checkMetaInfoValidityForMissingTopLevelElement("info")
  }

  test("checkMetaInfoValidity throws when info element is not a map") {
    val map = MetaInfoTest.get_metainfo_map_for_single_file
    val badMap = (map - "info") + ("info" -> "notamap")
    checkMetaInfoValidityForFailure(badMap, "info")
  }

  test("checkMetaInfoValidity throws when info/name element is not present") {
    checkMetaInfoValidityForMissingInfoElement("name")
  }

  test("checkMetaInfoValidity throws when name is not a string") {
    val map = putInfoElement(MetaInfoTest.get_metainfo_map_for_single_file, "name", 10)
    checkMetaInfoValidityForFailure(map, "name")
  }

  test("checkMetaInfoValidity throws when info/piece length element is not present") {
    checkMetaInfoValidityForMissingInfoElement("piece length")
  }

  test("checkMetaInfoValidity throws when piece length is not an int") {
    val map = putInfoElement(MetaInfoTest.get_metainfo_map_for_single_file, "piece length", "not an int")
    checkMetaInfoValidityForFailure(map, "piece length")
  }

  test("checkMetaInfoValidity throws when info/pieces element is not present") {
    checkMetaInfoValidityForMissingInfoElement("pieces")
  }

  test("checkMetaInfoValidity throws when pieces is not a string") {
    val map = putInfoElement(MetaInfoTest.get_metainfo_map_for_single_file, "pieces", 10)
    checkMetaInfoValidityForFailure(map, "pieces")
  }

  //single file only
  test("checkMetaInfoValidity throws when info/length element is not present") {
    checkMetaInfoValidityForMissingInfoElement("length")
  }

  test("checkMetaInfoValidity throws when info/length is not an integer") {
    val map = putInfoElement(MetaInfoTest.get_metainfo_map_for_single_file, "length", "not an int")
    checkMetaInfoValidityForFailure(map, "length")
  }

  //multi file only
  test("checkMetaInfoValidity throws when info/files is not present for multifile metainfo") {
    val map = MetaInfoTest.get_metainfo_map_for_multi_file
    val badMap = (map - "info") + ("info" -> (map.get("info").get.asInstanceOf[Map[String, Any]] - "files"))
    checkMetaInfoValidityForFailure(badMap, "files")
  }

  test("checkMetaInfoValidity throws when info/files is not a list") {
    val map = putInfoElement(MetaInfoTest.get_metainfo_map_for_multi_file, "files", "not a list")
    checkMetaInfoValidityForFailure(map, "files")
  }

  test("checkMetaInfoValidity throws when info/files list contains a non-dictionary") {
    val map = MetaInfoTest.get_metainfo_map_for_multi_file
    val fileList = map.get("info").get.asInstanceOf[Map[String, Any]].get("files").get.asInstanceOf[List[Any]]
    val bogusFileList = "not a dictionary" :: fileList
    val bogusMap = putInfoElement(map, "files", bogusFileList)
    checkMetaInfoValidityForFailure(bogusMap, "files")
  }

  test("checkMetaInfoValidity throws when info/files[X]/length is not present for multifile metainfo") {
    val fileDict = Map("not_length" -> 100, "path" -> List("a", "b"))
    val map = addFileDictToMultifileMetainfo(fileDict)
    checkMetaInfoValidityForFailure(map, "length")
  }

  test("checkMetaInfoValidity throws when info/files[X]/length is not an integer for multifile metainfo") {
    val fileDict = Map("length" -> "not an int", "path" -> List("a", "b"))
    val map = addFileDictToMultifileMetainfo(fileDict)
    checkMetaInfoValidityForFailure(map, "length")
  }

  test("checkMetaInfoValidity throws when info/files[X]/path is not present for multifile metainfo") {
    val fileDict = Map("length" -> 100, "not_path" -> List("a", "b"))
    val map = addFileDictToMultifileMetainfo(fileDict)
    checkMetaInfoValidityForFailure(map, "path")
  }

  test("checkMetaInfoValidity throws when info/files[X]/path is not a list for multifile metainfo") {
    val fileDict = Map("length" -> 100, "path" -> "not a list")
    val map = addFileDictToMultifileMetainfo(fileDict)
    checkMetaInfoValidityForFailure(map, "path")
  }

  test("checkMetaInfoValidity throws when info/files[X]/path is an empty list") {
    val fileDict = Map("length" -> 100, "path" -> List())
    val map = addFileDictToMultifileMetainfo(fileDict)
    checkMetaInfoValidityForFailure(map, "path")
  }

  test("checkMetaInfoValidity throws when info/files[X]/path contains a non-string") {
    val fileDict = Map("length" -> 100, "path" -> List("path1", 123))
    val map = addFileDictToMultifileMetainfo(fileDict)
    checkMetaInfoValidityForFailure(map, "path")
  }

  def addFileDictToMultifileMetainfo(fileDict: Map[String, Any]): Map[String, Any] = {
    val map = MetaInfoTest.get_metainfo_map_for_multi_file
    val fileList = map.get("info").get.asInstanceOf[Map[String, Any]].get("files").get.asInstanceOf[List[Any]]
    val newFileList = fileDict :: fileList
    putInfoElement(map, "files", newFileList)
  }

  //appears to be both single & multifile
  test("checkMetaInfoValidity throws when both length & files are present") {
    val map = putInfoElement(MetaInfoTest.get_metainfo_map_for_single_file, "files", "no matter")
    checkMetaInfoValidityForFailure(map, "files")
  }

  def putInfoElement(map: Map[String, Any], key: String, value: Any) = {
    (map - "info") + ("info" -> (map.get("info").get.asInstanceOf[Map[String, Any]] + (key -> value)))
  }

  def checkMetaInfoValidityForMissingTopLevelElement(missing: String) {
    checkMetaInfoValidityForFailure(MetaInfoTest.get_metainfo_map_for_single_file - missing, missing)
  }

  def checkMetaInfoValidityForMissingInfoElement(missing: String) {
    val map = MetaInfoTest.get_metainfo_map_for_single_file
    val badMap = (map - "info") + ("info" -> (map.get("info").get.asInstanceOf[Map[String, Any]] - missing))
    checkMetaInfoValidityForFailure(badMap, missing)
  }

  def checkMetaInfoValidityForFailure(input: Map[String, Any], expectedMissing: String) {
    val dict = (new BEncoder).encodeMap(input)
    val caught = evaluating { MetaInfoValidator.validate(dict) } should produce [IllegalArgumentException]
    caught.getMessage should include (expectedMissing)
  }

  test("checkMetaInfoValidity does not throw exception for a valid single file MetaInfo file") {
    val dict = (new BDecoder).decodeMap(MetaInfoTest.get_metainfo_file_contents)
    MetaInfoValidator.validate(dict)
  }

  test("checkMetaInfoValidity does not throw exception for a valid multifile MetaInfo file") {
    val dict = (new BDecoder).decodeMap(MetaInfoTest.get_metainfo_file_contents_multifile)
    MetaInfoValidator.validate(dict)
  }
}
