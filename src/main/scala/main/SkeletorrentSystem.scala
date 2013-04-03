package main

import akka.actor.{Props, ActorSystem}
import spray.client.HttpClient
import tracker.{TrackerAnnouncerComponent, HttpClientComponent}
import torrent.peer.{PeerAccepterComponent, PeerAccepterTcp}
import torrent.TorrentFactoryComponent

class SkeletorrentSystem(conf: Conf, system: ActorSystem)
  extends HttpClientComponent
  with TrackerAnnouncerComponent
  with TorrentFactoryComponent
  with PeerAccepterComponent {

  override val httpClient = system.actorOf(Props(new HttpClient))
  override val trackerAnnouncer = system.actorOf(Props(new TrackerAnnouncer), name="tracker-announcer")
  override val torrentFactory = new TorrentFactory()
  override val peerAccepter = system.actorOf(Props[PeerAccepterTcp], name="peer-accepter")
}
