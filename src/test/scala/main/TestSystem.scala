package main

import tracker.{TrackerAnnouncerComponent, HttpClientComponent}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import torrent.TorrentFactoryComponent
import torrent.peer.{OutboundPeerFactory, OutboundPeerFactoryComponent, PeerAccepterComponent}
import akka.actor.ActorSystem
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers

class TestSystem(_system: ActorSystem)
    extends TestKit(_system)
    with HttpClientComponent
    with TrackerAnnouncerComponent
    with TorrentFactoryComponent
    with OutboundPeerFactoryComponent
    with PeerAccepterComponent

    with ImplicitSender
    with FunSuite
    with ShouldMatchers
    with BeforeAndAfterAll
    with MockitoSugar {

  def this() = this(ActorSystem("TestSystem"))
  def testActorSystem = _system

  override def afterAll() { _system.shutdown() }

  val httpClientProbe = TestProbe()
  override val httpClient = httpClientProbe.ref

  val trackerAnnouncerProbe = TestProbe()
  override val trackerAnnouncer = trackerAnnouncerProbe.ref

  val peerAccepterProbe = TestProbe()
  override val peerAccepter = peerAccepterProbe.ref

  override val torrentFactory = mock[TorrentFactory]
  override val outboundPeerFactory = mock[OutboundPeerFactory]
}
