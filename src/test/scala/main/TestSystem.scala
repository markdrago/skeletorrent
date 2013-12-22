package main

import tracker.{TrackerAnnouncerComponent, HttpClientComponent}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import torrent.TorrentFactoryComponent
import torrent.peer.{OutboundPeerFactory, OutboundPeerFactoryComponent}
import akka.actor.ActorSystem
import org.scalatest.{FunSuiteLike, BeforeAndAfterAll, Matchers}
import org.scalatest.mock.MockitoSugar

class TestSystem(_system: ActorSystem)
    extends TestKit(_system)
    with HttpClientComponent
    with TrackerAnnouncerComponent
    with TorrentFactoryComponent
    with OutboundPeerFactoryComponent

    with ImplicitSender
    with FunSuiteLike
    with Matchers
    with BeforeAndAfterAll
    with MockitoSugar {

  def this() = this(ActorSystem("TestSystem"))
  def testActorSystem = _system

  override def afterAll() { _system.shutdown() }

  val httpClientProbe = TestProbe()
  override val httpClient = httpClientProbe.ref

  val trackerAnnouncerProbe = TestProbe()
  override val trackerAnnouncer = trackerAnnouncerProbe.ref

  override val torrentFactory = mock[TorrentFactory]
  override val outboundPeerFactory = mock[OutboundPeerFactory]
}
