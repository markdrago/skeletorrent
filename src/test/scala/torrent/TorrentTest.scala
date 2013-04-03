package torrent

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.TestKit
import akka.actor.{Props, ActorSystem}
import bencoding.messages.{MetaInfo, MetaInfoSample}
import main.TestSystem
import peer.TorrentStateTag
import torrent.Torrent.TorrentStartMsg
import spray.io.IOBridge.Bind
import concurrent.duration._
import java.net.InetSocketAddress
import tracker.TrackerAnnouncer.{TrackerRequest, TrackerAnnounceRequestMsg}
import spray.io.IOServer.Bound

class TorrentTest(_system: ActorSystem) extends TestKit(_system) with TestSystem
    with FunSuite with ShouldMatchers with BeforeAndAfterAll {

  //need to provide a no-argument constructor
  def this() = this(ActorSystem("TorrentTest"))

  //since we are making our own actor system, make sure we use the same one
  //within TestSystem by overriding testActorSystem here
  override def testActorSystem = _system

  override def afterAll() { _system.shutdown() }

  val metaInfo = MetaInfo(MetaInfoSample.get_metainfo_file_contents)

  def get_torrent = _system.actorOf(Props(
    new Torrent(
      6881,
      "abc",
      metaInfo,
      peerAccepter,
      trackerAnnouncer
    )
  ))

  test("torrent sends binding message to peerAccepterProbe after receiving init message") {
    val torrent = get_torrent
    torrent ! TorrentStartMsg()
    peerAccepterProbe.expectMsg[Bind](1000.millis,
      Bind(
        new InetSocketAddress(6881),
        100,
        TorrentStateTag(torrent, metaInfo.infoHash, "abc")
      )
    )
  }

  test("torrent sends tracker announce request after receiving bound message") {
    //get torrent and put it in to awaitingBinding state
    val torrent = get_torrent
    torrent ! TorrentStartMsg()
    peerAccepterProbe.expectMsgType[Bind](1000.millis)

    //send bound message and check for resulting message to tracker announcer
    torrent ! Bound(new InetSocketAddress(6881), null)
    trackerAnnouncerProbe.expectMsg[TrackerAnnounceRequestMsg](1000.millis,
      TrackerAnnounceRequestMsg(TrackerRequest(
        metaInfo.trackerUrl,
        metaInfo.infoHash,
        "abc",
        6881,
        0, 0, 0
      ))
    )
  }
}
