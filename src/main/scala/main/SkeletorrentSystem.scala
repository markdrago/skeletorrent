package main

import akka.actor.{Props, ActorSystem}
import spray.client.HttpClient
import tracker.{TrackerAnnouncerComponent, HttpClientComponent}
import torrent.peer.PeerAccepterTcp

class SkeletorrentSystem(system: ActorSystem)
  extends HttpClientComponent
  with TrackerAnnouncerComponent {

  override val httpClient = system.actorOf(Props(new HttpClient))
  override val trackerAnnouncer = system.actorOf(Props(new TrackerAnnouncer), name="tracker-announcer")

  //create non-cake-pattern actor singletons
  system.actorOf(Props[PeerAccepterTcp], name="peer-accepter")
}
