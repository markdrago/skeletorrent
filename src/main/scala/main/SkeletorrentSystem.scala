package main

import akka.actor.{Props, ActorSystem}
import akka.io.IO

import spray.can.Http
import tracker.{TrackerAnnouncerComponent, HttpClientComponent}
import torrent.peer.{OutboundPeerFactory, OutboundPeerFactoryComponent}
import torrent.TorrentFactoryComponent

class SkeletorrentSystem(conf: Conf, system: ActorSystem)
    extends HttpClientComponent
    with TrackerAnnouncerComponent
    with TorrentFactoryComponent
    with OutboundPeerFactoryComponent {

  override val httpClient = IO(Http)(system)
  override val trackerAnnouncer = system.actorOf(Props(new TrackerAnnouncer))
  override val torrentFactory = new TorrentFactory()
  override val outboundPeerFactory = new OutboundPeerFactory()
}
