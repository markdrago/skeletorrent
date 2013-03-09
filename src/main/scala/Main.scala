package main.scala

import org.rogach.scallop._
import akka.actor.{Props, ActorSystem}
import torrent.{InjectMetainfoFileMsg, Torrent}

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val metainfoFileName = trailArg[String]("metainfoFileName", required=true)
  verify
}

object Main {
  def main(args: Array[String]) {
    val conf = new Conf(args)

    val actorSystem = ActorSystem("sk")
    new SkeletorrentSystem(actorSystem)

    val torrentActor = actorSystem.actorOf(Props(new Torrent()), name="torrent")
    torrentActor ! InjectMetainfoFileMsg(conf.metainfoFileName())
  }
}
