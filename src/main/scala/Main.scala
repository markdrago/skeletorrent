package main.scala

import org.rogach.scallop._

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val metainfoFileName = trailArg[String]("metainfoFileName", required=true)
  verify
}

object Main {
  def main(args: Array[String]) {
    val conf = new Conf(args)
    val torrent = new Torrent(conf.metainfoFileName())
  }
}
