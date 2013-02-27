package main.scala

import akka.util.ByteString
import java.io.File
import org.apache.commons.io.FileUtils

class Torrent(val metainfoFileName: String) {
  val metainfo = MetaInfo(readFile(metainfoFileName))
  println(metainfo.encodedInfoHash)

  def readFile(filename: String): ByteString = {
    ByteString(FileUtils.readFileToByteArray(new File(filename)))
  }
}
