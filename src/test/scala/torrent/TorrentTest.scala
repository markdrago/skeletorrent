package torrent

import akka.actor.{ActorContext, Props, ActorSystem}
import bencoding.messages.{TrackerPeerDetails, TrackerResponse, MetaInfo, MetaInfoSample}
import main.TestSystem
import peer.TorrentStateTag
import torrent.Torrent.TorrentStartMsg
import spray.io.IOBridge.Bind
import concurrent.duration._
import java.net.InetSocketAddress
import tracker.TrackerAnnouncer.{TrackerAnnounceResponseMsg, TrackerRequest, TrackerAnnounceRequestMsg}
import spray.io.IOServer.Bound
import akka.util.ByteString
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => same, any}

class TorrentTest(_system: ActorSystem) extends TestSystem(_system) {
  def this() = this(ActorSystem("TrackerAnnouncerTest"))

  val peerId = "TestTorrentPeerId"
  val metaInfo = MetaInfo(MetaInfoSample.get_metainfo_file_contents)

  def get_torrent = testActorSystem.actorOf(Props(
      new Torrent(
        6881,
        peerId,
        metaInfo,
        peerAccepter,
        trackerAnnouncer,
        outboundPeerFactory
      )
    ))

  test("torrent sends binding message to peerAccepterProbe after receiving init message") {
    val torrent = get_torrent
    torrent ! TorrentStartMsg()
    peerAccepterProbe.expectMsg[Bind](1000.millis,
      Bind(
        new InetSocketAddress(6881),
        100,
        TorrentStateTag(torrent, metaInfo.infoHash, peerId)
      )
    )
  }

  test("torrent sends tracker announce request after receiving bound message") {
    //get torrent and put it in awaitingBinding state
    val torrent = get_torrent
    torrent ! TorrentStartMsg()

    //send bound message and check for resulting message to tracker announcer
    torrent ! Bound(new InetSocketAddress(6881), null)
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
    //get torrent and put it in awaitingBinding state
    val torrent = get_torrent
    torrent ! TorrentStartMsg()
    torrent ! Bound(null, null)

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
}
