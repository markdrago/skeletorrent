package test.scala

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import main.scala.Conf

class MainTest extends FunSuite with ShouldMatchers {
  test("conf can find metainfo filename with no other parameters") {
    val conf = new Conf(List("/tmp/metainfo.torrent").toSeq)
    conf.metainfoFileName should be ('defined)
    conf.metainfoFileName() should be ("/tmp/metainfo.torrent")
  }
}
