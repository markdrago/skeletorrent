package main

import tracker.{TrackerAnnouncerComponent, HttpClientComponent}
import akka.testkit.{TestKit, TestProbe}
import torrent.TorrentFactoryComponent
import torrent.peer.{OutboundPeerFactory, OutboundPeerFactoryComponent, PeerAccepterComponent}
import akka.actor.ActorSystem
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar

//TODO: start to break apart the wiring of this
//TODO: at least separate wiring stuff from test running stuff

trait TestSystem
    extends HttpClientComponent
    with TrackerAnnouncerComponent
    with TorrentFactoryComponent
    with OutboundPeerFactoryComponent
    with PeerAccepterComponent
    with FunSuite
    with MockitoSugar {
  this: TestKit =>

  def testActorSystem: ActorSystem = ActorSystem("TestSystem")

  val httpClientProbe = TestProbe()
  override val httpClient = httpClientProbe.ref

  val trackerAnnouncerProbe = TestProbe()
  override val trackerAnnouncer = trackerAnnouncerProbe.ref

  val peerAccepterProbe = TestProbe()
  override val peerAccepter = peerAccepterProbe.ref

  override val torrentFactory = mock[TorrentFactory]
  override val outboundPeerFactory = mock[OutboundPeerFactory]
}
