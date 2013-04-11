package torrent.peer

import main.TestSystem
import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.TestProbe
import akka.testkit.EventFilter
import akka.util.ByteString
import spray.io.IOBridge.{Connection, Connect}
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.Status.Failure
import spray.io.IOClientConnection.Connected
import sun.org.mozilla.javascript.commonjs.module.provider.DefaultUrlConnectionExpiryCalculator
import spray.io.IOBridge
import torrent.Torrent.RegisterPeerWithTorrent

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

    val peer = testActorSystem.actorOf(
      Props(
        new OutboundPeer(connection.ref, tag, otherPeerId, host, port)
      )
    )

    val fakeConnection = FakeConnection(
      null,
      TestProbe().ref,
      TestProbe().ref,
      TestProbe().ref,
      tag
    )
  }

  test("OutboundPeer preStart sends message to connect to peer") {
    new Fixture {
      connection.expectMsg[Connect](1000.millis, Connect(host, port, tag))
    }
  }

  test("OutboundPeer on failure to connect logs a warning message") {
    new Fixture {
      EventFilter.warning(start = "Failed to connect to outbound peer:") intercept {
        peer ! Failure
      }
    }
  }

  test("OutboundPeer registers with torrent after successful connection") {
    new Fixture {
      peer ! Connected(fakeConnection)
      torrent.expectMsg[RegisterPeerWithTorrent](
        1000.millis,
        RegisterPeerWithTorrent(peer)
      )
    }
  }

}
