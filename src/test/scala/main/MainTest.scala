package main

import org.scalatest.{Matchers, FunSuite}

class MainTest extends FunSuite with Matchers {
  test("argparser can find metainfo filename with no other parameters") {
    val argparser = new ArgParser(List("/tmp/metainfo.torrent").toSeq)
    argparser.metainfoFileName should be('defined)
    argparser.metainfoFileName() should be("/tmp/metainfo.torrent")
  }
}
