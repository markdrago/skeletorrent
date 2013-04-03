package main

import tracker.{TrackerAnnouncerComponent, HttpClientComponent}
import akka.testkit.{TestKit, TestProbe}
import torrent.TorrentFactoryComponent
import torrent.peer.PeerAccepterComponent
import akka.actor.ActorSystem

trait TestSystem
    extends HttpClientComponent
    with TrackerAnnouncerComponent
    with TorrentFactoryComponent
    with PeerAccepterComponent {
  this: TestKit =>

  def testActorSystem: ActorSystem = ActorSystem("TestSystem")

  val httpClientProbe = TestProbe()
  override val httpClient = httpClientProbe.ref

  val trackerAnnouncerProbe = TestProbe()
  override val trackerAnnouncer = trackerAnnouncerProbe.ref

  val peerAccepterProbe = TestProbe()
  override val peerAccepter = peerAccepterProbe.ref

  //TODO: add mocking library and wire in a mock here instead
  val torrentFactory = new TorrentFactory
}
