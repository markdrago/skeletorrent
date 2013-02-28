package torrent

import main.scala.MetaInfo
import akka.actor.Actor
import akka.util.ByteString
import org.apache.commons.io.FileUtils
import java.io.File

class Torrent(val metainfoFileName: String) extends Actor {
  var metainfo: MetaInfo = null

  override def preStart {
    initMetaInfoFile()
  }

  def receive = {
    case _ => Unit
  }

  def initMetaInfoFile() {
    this.metainfo = MetaInfo(readFile(metainfoFileName))
    println(this.metainfo.encodedInfoHash)
  }

  def readFile(filename: String): ByteString = {
    ByteString(FileUtils.readFileToByteArray(new File(filename)))
  }
}
