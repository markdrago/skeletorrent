package main

import akka.actor.{Props, ActorSystem}
import spray.client.HttpClient
import tracker.{TrackerAnnouncerComponent, HttpClientComponent}
import torrent.peer.{OutboundPeerFactory, OutboundPeerFactoryComponent, PeerAccepterComponent, PeerAccepterTcp}
import torrent.TorrentFactoryComponent

class SkeletorrentSystem(conf: Conf, system: ActorSystem)
  extends HttpClientComponent
  with TrackerAnnouncerComponent
  with TorrentFactoryComponent
  with OutboundPeerFactoryComponent
  with PeerAccepterComponent {

  override val httpClient = system.actorOf(Props(new HttpClient))
  override val trackerAnnouncer = system.actorOf(Props(new TrackerAnnouncer))
  override val torrentFactory = new TorrentFactory()
  override val peerAccepter = system.actorOf(Props[PeerAccepterTcp])
  override val outboundPeerFactory = new OutboundPeerFactory()
}
