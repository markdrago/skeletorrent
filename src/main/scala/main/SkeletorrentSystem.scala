package main

import akka.actor.{ActorSystem, Props}
import spray.client.HttpClient
import tracker.{TrackerAnnouncerComponent, HttpClientComponent}

class SkeletorrentSystem(system: ActorSystem)
  extends HttpClientComponent
  with TrackerAnnouncerComponent {

  override val httpClient = system.actorOf(Props(new HttpClient))
  override val trackerAnnouncer = system.actorOf(Props(new TrackerAnnouncer), name="tracker-announcer")
}
