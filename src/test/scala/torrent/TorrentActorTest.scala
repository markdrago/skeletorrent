package torrent

import akka.actor.{ActorRef, ActorSystem}
import akka.io.Tcp.Bound
import akka.testkit.{TestKit, ImplicitSender, TestProbe, TestActorRef}
import akka.util.ByteString
import bencoding.messages.{AvailablePeerDetails, MetaInfo, MetaInfoSample}
import concurrent.duration._
import java.net.InetSocketAddress
import org.scalatest.{FunSuiteLike, Matchers}
import scala.language.postfixOps
import torrent.TorrentActor.AvailablePeerSet
import torrent.peer.{PeerType, Peer}
import tracker.TrackerActor
import tracker.TrackerActor.TrackerStart
import utils.ProbeForwarderProps._

class TorrentActorTest
  extends TestKit(ActorSystem("TorrentActorTest"))
  with ImplicitSender
  with FunSuiteLike
  with Matchers {

  val metaInfo = MetaInfo(MetaInfoSample.get_metainfo_file_contents)

  val tcpManagerProbe = TestProbe()
  val httpManagerProbe = TestProbe()

  //TODO: figure out how to support multiple outbound peers being created by torrent actor
  val peerProbe = TestProbe()

  def peerTestProbeFactory: Peer.PeerPropsFactory = (
    _: ActorRef,
    _: PeerType,
    _: ByteString,
    _: ByteString,
    _: Option[ByteString]) =>
    peerProbe.props

  val trackerActorProbe = TestProbe()

  def trackerActorTestProbeFactory: TrackerActor.TrackerActorPropsFactory = (
    _: ActorRef,
    _: String,
    _: ByteString,
    _: String,
    _: Int) =>
    trackerActorProbe.props

  def get_torrent = TestActorRef(
    new TorrentActor(
      6881,
      metaInfo,
      tcpManagerProbe.ref,
      httpManagerProbe.ref,
      trackerActorTestProbeFactory,
      peerTestProbeFactory
    )
  )

  //TODO: verify that Torrent sends a Bind message to tcpManager correctly

  test("generatePeerId generates a string with proper prefix and length 20") {
    val peerId = get_torrent.underlyingActor.peerId
    peerId should have length 20
    peerId should startWith("-SK0001-")
  }

  test("torrent sends tracker announce request after receiving bound message") {
    //get torrent and put it in awaitingBinding state
    val torrent = get_torrent
    //    torrent ! TorrentStartMsg()

    //send bound message and check for resulting message to tracker announcer
    torrent ! Bound(new InetSocketAddress(6881))
    trackerActorProbe.expectMsg(1 second, TrackerStart)
  }

  test("torrent tries to connect to peers after receiving tracker response") {
    //get torrent and put it in steady state
    val torrent = get_torrent
    //    torrent ! TorrentStartMsg()
    torrent ! Bound(null)

    //send tracker response message and check for interaction with outbound peer factory mock
    val peers = Set(AvailablePeerDetails(ByteString("peerid"), "1.2.3.4", 6881))
    torrent ! AvailablePeerSet(peers)

    //TODO: should probably assert something
    //val expectedTag = TorrentStateTag(torrent, metaInfo.infoHash, torrent.underlyingActor.peerId)
    //val initMessage = outboundPeerProbe.expectMsgClass(classOf[OutboundPeerInit])
    /*    initMessage.host should be("1.2.3.4")
        initMessage.otherPeerId should be(ByteString("peerid"))
        initMessage.port should be(6881)
        initMessage.tag should be(expectedTag)
        */
  }

  //TODO: test that torrent maintains pendingConnections properly (adds & removes)

  //TODO: test that torrent maintains peers set properly
  /*
  test("torrent remembers peers which register with it") {
    //get torrent and put it in steady state
    val torrent = get_torrent
    //    torrent ! TorrentStartMsg()
    torrent ! Bound(null)

    torrent.underlyingActor.peers should be('empty)

    //send tracker peer registration message
    val peer = TestProbe()
    torrent ! RegisterPeerWithTorrent(peer.ref)

    torrent.underlyingActor.peers should contain(peer.ref)
    torrent.underlyingActor.peers should have length 1
  }
  */
}
