package torrent.peer

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef, TestProbe}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, FunSuiteLike}
import scala.concurrent.duration._
import torrent.TorrentActor.RegisterPeerWithTorrent

//TODO: this suite clearly needs some attention

class PeerTest
  extends TestKit(ActorSystem(
    "PeerTest",
    ConfigFactory.parseString( """akka.loggers = ["akka.testkit.TestEventListener"]""")))
  with ImplicitSender
  with FunSuiteLike
  with Matchers {

  private trait Fixture {
    val torrent = TestProbe()
    val connection = TestProbe()
    val infoHash = ByteString("infoHash")
    val peerId = "peerId"
    val otherPeerId = ByteString("otherPeerId")
    val host = "1.2.3.4"
    val port = 6881

    val tcpManagerProbe = TestProbe()

    /*
    def getPeer: ActorRef = {
      val outbound = system.actorOf(Props(classOf[OutboundPeer]))
      //outbound ! OutboundPeerInit(tag, otherPeerId, host, port)
      outbound
    }
    */

    def getTestPeerRef: TestActorRef[OutboundPeer] =
      TestActorRef(new OutboundPeer(tcpManagerProbe.ref, otherPeerId, host, port, infoHash))
  }

  /*
  test("registerPeerWithTorrent actually does what it claims") {
    new Fixture {
      within(250.millis) {
        val peer = getTestPeerRef
        peer.underlyingActor.registerPeerWithTorrent(tag)
        torrent.expectMsg[RegisterPeerWithTorrent](
          RegisterPeerWithTorrent(peer)
        )
      }
    }
  }
  */
}
