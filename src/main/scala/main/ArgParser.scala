package main

import org.rogach.scallop.ScallopConf

class ArgParser(args: Seq[String]) extends ScallopConf(args) {
  val metainfoFileName = trailArg[String]("metainfoFileName", required = true)
}
