package torrent.peer

import akka.actor.{Props, ActorSystem}
import main.TestSystem
import com.typesafe.config.ConfigFactory
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.ByteString
import torrent.Torrent.RegisterPeerWithTorrent
import scala.concurrent.duration._

class PeerTest(_system: ActorSystem) extends TestSystem(_system) {
  def this() = this(
    ActorSystem(
      "PeerTest",
      ConfigFactory.parseString("""akka.loggers = ["akka.testkit.TestEventListener"]""")
    )
  )

  private trait Fixture {
    val torrent = TestProbe()
    val connection = TestProbe()
    val infoHash = ByteString("infoHash")
    val peerId = "peerId"
    val otherPeerId = ByteString("otherPeerId")
    val host = "1.2.3.4"
    val port = 6881
    val tag = TorrentStateTag(torrent.ref, infoHash, peerId)

    def getPeer = testActorSystem.actorOf(
      Props(
        new OutboundPeer(tag, otherPeerId, host, port).asInstanceOf[Peer]
      )
    )

    def getTestPeerRef = TestActorRef[Peer](
      Props(
        new OutboundPeer(tag, otherPeerId, host, port)
      )
    )
  }

  test("registerPeerWithTorrent actually does what it claims") {
    new Fixture {
      within(100.millis) {
        val peer = getTestPeerRef
        peer.underlyingActor.registerPeerWithTorrent(tag)
        torrent.expectMsg[RegisterPeerWithTorrent](
          RegisterPeerWithTorrent(peer)
        )
      }
    }
  }
}
