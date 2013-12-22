package torrent

import akka.actor.{Actor, ActorContext, Props, ActorSystem}
import bencoding.messages.{TrackerPeerDetails, TrackerResponse, MetaInfo, MetaInfoSample}
import main.TestSystem
import peer.TorrentStateTag
import torrent.Torrent.{RegisterPeerWithTorrent, TorrentStartMsg}
import concurrent.duration._
import java.net.InetSocketAddress
import tracker.TrackerAnnouncer.{TrackerAnnounceResponseMsg, TrackerRequest, TrackerAnnounceRequestMsg}
import akka.util.ByteString
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => same, any}
import akka.testkit.{TestProbe, TestActorRef}
import akka.io.Tcp.Bound

class TorrentTest(_system: ActorSystem) extends TestSystem(_system) {
  def this() = this(ActorSystem("TrackerAnnouncerTest"))

  val peerId = "TestTorrentPeerId"
  val metaInfo = MetaInfo(MetaInfoSample.get_metainfo_file_contents)

  def get_torrent = TestActorRef(
      new Torrent(
        6881,
        peerId,
        metaInfo,
        trackerAnnouncer,
        outboundPeerFactory
      )
    )

  //TODO: inject fake IO(Tcp) and verify that Torrent sends a Bind message correctly

  test("torrent sends tracker announce request after receiving bound message") {
    //get torrent and put it in awaitingBinding state
    val torrent = get_torrent
    torrent ! TorrentStartMsg()

    //send bound message and check for resulting message to tracker announcer
    torrent ! Bound(new InetSocketAddress(6881))
    trackerAnnouncerProbe.expectMsg[TrackerAnnounceRequestMsg](1000.millis,
      TrackerAnnounceRequestMsg(TrackerRequest(
        metaInfo.trackerUrl,
        metaInfo.infoHash,
        peerId,
        6881,
        0, 0, 0
      ))
    )
  }

  test("torrent tries to connect to peers after receiving tracker response") {
    //get torrent and put it in steady state
    val torrent = get_torrent
    torrent ! TorrentStartMsg()
    torrent ! Bound(null)

    //send tracker response message and check for interaction with outbound peer factory mock
    val peers = TrackerPeerDetails(ByteString("peerid"), "1.2.3.4", 6881) :: Nil
    torrent ! TrackerAnnounceResponseMsg(TrackerResponse(180, peers))

    val expectedTag = TorrentStateTag(torrent, metaInfo.infoHash, peerId)
    verify(outboundPeerFactory).create(
      any[ActorContext],
      same(expectedTag),
      same(ByteString("peerid")),
      same("1.2.3.4"),
      same(6881)
    )
  }

  test("torrent remembers peers which register with it") {
    //get torrent and put it in stead state
    val torrent = get_torrent
    torrent ! TorrentStartMsg()
    torrent ! Bound(null)

    torrent.underlyingActor.peers should be ('empty)

    //send tracker peer registration message
    val peer = TestProbe()
    torrent ! RegisterPeerWithTorrent(peer.ref)

    torrent.underlyingActor.peers should contain (peer.ref)
    torrent.underlyingActor.peers should have length(1)
  }
}
