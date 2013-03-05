package utils

import akka.util.ByteString
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.commons.codec.net.URLCodec

object Utils {
  def urlEncode(bs: ByteString): String = {
    new String((new URLCodec("UTF-8")).encode(bs.toArray))
  }

  def readFile(filename: String): ByteString = {
    ByteString(FileUtils.readFileToByteArray(new File(filename)))
  }
}
