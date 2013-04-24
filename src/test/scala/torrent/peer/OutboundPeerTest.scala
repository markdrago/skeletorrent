package torrent.peer

import main.TestSystem
import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe, EventFilter}
import akka.util.ByteString
import spray.io.IOBridge.{Connect, Connection}
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.Status.Failure
import spray.io.IOClientConnection.Connected
import spray.io.IOBridge
import torrent.Torrent.RegisterPeerWithTorrent
import spray.io.IOConnection.Send

class OutboundPeerTest(_system: ActorSystem) extends TestSystem(_system) {
  def this() = this(
    ActorSystem(
      "OutboundPeerTest",
      ConfigFactory.parseString("""akka.loggers = ["akka.testkit.TestEventListener"]""")
    )
  )

  case class FakeConnection(
    key: IOBridge.Key,
    handler: ActorRef,
    ioBridge: ActorRef,
    commander: ActorRef,
    tag: Any
  ) extends Connection

  private trait Fixture {
    val torrent = TestProbe()
    val connection = TestProbe()
    val infoHash = ByteString("infoHash")
    val peerId = "peerId"
    val otherPeerId = ByteString("otherPeerId")
    val host = "1.2.3.4"
    val port = 6881
    val tag = TorrentStateTag(torrent.ref, infoHash, peerId)

    val fakeConnection = FakeConnection(
      null,
      TestProbe().ref,
      TestProbe().ref,
      TestProbe().ref,
      tag
    )

    def getPeer = testActorSystem.actorOf(
      Props(
        new OutboundPeer(connection.ref, tag, otherPeerId, host, port)
      )
    )

    def getTestPeerRef = TestActorRef[OutboundPeer](
      Props(
        new OutboundPeer(connection.ref, tag, otherPeerId, host, port)
      )
    )
  }

  test("OutboundPeer preStart sends message to connect to peer") {
    new Fixture {
      within(100.millis) {
        getPeer
        connection.expectMsg[Connect](Connect(host, port, tag))
      }
    }
  }

  test("OutboundPeer on failure to connect logs a warning message") {
    new Fixture {
      within(100.millis) {
        EventFilter.warning(start = "Failed to connect to outbound peer:") intercept {
          getPeer ! Failure
        }
      }
    }
  }

  test("OutboundPeer registers with torrent after successful connection") {
    new Fixture {
      within(100.millis) {
        val peer = getPeer
        peer ! Connected(fakeConnection)
        torrent.expectMsg[RegisterPeerWithTorrent](
          RegisterPeerWithTorrent(peer)
        )
      }
    }
  }

  test("OutboundPeer throws IAE and logs when given a connection with a bogus tag") {
    new Fixture {
      val bogusConnection = fakeConnection.copy(tag = "not a real tag")
      EventFilter.warning(message = "Unexpected tag type found in new outbound peer connection.") intercept {
        intercept[IllegalArgumentException] {
          getTestPeerRef.underlyingActor.connectionComplete(bogusConnection)
        }
      }
    }
  }

  test("OutboundPeer sends handshake initiation to newly connected peer") {
    new Fixture {
      within(100.millis) {
        val peer = getPeer
        connection.expectMsgType[Connect]
        peer ! Connected(fakeConnection)
        connection.expectMsgType[Send]
      }
    }
  }

  test("OutboundPeer produces correct bittorrent protocol handshake") {
    new Fixture {
      val peerActor = getTestPeerRef.underlyingActor

      val result = peerActor.handshake(tag)

      val header = "BitTorrent protocol"
      assert(result.startsWith(List(19.toByte)))
      assert(result.drop(1).startsWith(header))
      assert(result.drop(header.length + 1).startsWith(Array.fill(8)(0.toByte)))
      assert(result.drop(header.length + 9).startsWith(tag.infoHash))
      assert(result.drop(header.length + tag.infoHash.length + 9).startsWith(tag.peerId))
    }
  }
}
