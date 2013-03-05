package main.scala

import org.rogach.scallop._
import akka.actor.{Props, ActorSystem}
import torrent.{InjectMetainfoFileMsg, Torrent}
import spray.client.HttpClient
import tracker.TrackerAnnouncer

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val metainfoFileName = trailArg[String]("metainfoFileName", required=true)
  verify
}

object Main {
  def main(args: Array[String]) {
    val conf = new Conf(args)

    val system = ActorSystem("sk")
    spawnSingletonActors(system)

    val torrentActor = system.actorOf(Props(new Torrent()), name="torrent")
    torrentActor ! InjectMetainfoFileMsg(conf.metainfoFileName())
  }

  def spawnSingletonActors(system: ActorSystem) {
    system.actorOf(Props(new HttpClient), "http-client")
    system.actorOf(Props(new TrackerAnnouncer), "tracker-announcer")
  }
}