package tracker

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe, ImplicitSender, TestKit}
import akka.util.ByteString
import bencoding.messages.AvailablePeerDetails
import concurrent.duration._
import org.scalatest.{Matchers, FunSuiteLike}
import scala.Some
import scala.language.postfixOps
import spray.http._
import tracker.TrackerActor._
import utils.ParentChildMediator
import torrent.TorrentActor.AvailablePeerSet

class TrackerActorTest
  extends TestKit(ActorSystem("TrackerActorTest"))
  with ImplicitSender
  with FunSuiteLike
  with Matchers {

  val httpManagerProbe = TestProbe()

  val baseUrl = "http://www.legaltorrents.com:7070/announce"
  val eventType: Option[AnnounceEvent] = None
  val infoHash = ByteString("info Hash&Here")
  val peerId = "peerIdHere"
  val port = 6881

  private trait Fixture {
    def trackerActor(customBaseUrl: String) = TestActorRef(new TrackerActor(httpManagerProbe
      .ref, customBaseUrl, infoHash, peerId, port))
  }

  private trait MediatorFixture {
    val fosterParentProbe = TestProbe()
    val trackerActor = ParentChildMediator(fosterParentProbe.ref, TrackerActor
      .props(httpManagerProbe.ref, baseUrl, infoHash, peerId, port))
  }

  test("TrackerAnnouncer replies with TrackerAnnounceResponseMsg on success") {
    new MediatorFixture {
      trackerActor ! TrackerStart
      httpManagerProbe.expectMsgType[HttpRequest](1 second)
      httpManagerProbe.reply(successfulHttpResponse)
      fosterParentProbe.expectMsg(successfulTrackerResponseMsg)
    }
  }

  test("TrackerAnnouncer replies with TrackerFailure after failed HTTP response") {
    new MediatorFixture {
      trackerActor ! TrackerStart
      httpManagerProbe.expectMsgType[HttpRequest](1 second)
      httpManagerProbe.reply(failedHttpResponse)
      fosterParentProbe.expectMsgType[TrackerFailure]
    }
  }

  test("prepareRequestUrl generates expected Url in simple situation") {
    new Fixture {
      val actor = trackerActor(baseUrl)
      val result = actor.underlyingActor.prepareRequestUrl(None)
      result should be(
        "http://www.legaltorrents.com:7070/announce?" +
          "info_hash=info+Hash%26Here&" +
          s"peer_id=peerIdHere&" +
          s"port=6881" +
          "&uploaded=0" +
          "&downloaded=0" +
          "&left=0"
      )
    }
  }

  test("prepareRequestUrl generates expected Url if announce URL already has parameters") {
    new Fixture {
      val actor = trackerActor("http://www.legaltorrents.com:7070/announce?key=1")
      val result = actor.underlyingActor.prepareRequestUrl(None)
      result should be(
        "http://www.legaltorrents.com:7070/announce?key=1&" +
          "info_hash=info+Hash%26Here&" +
          s"peer_id=peerIdHere&" +
          s"port=6881" +
          "&uploaded=0" +
          "&downloaded=0" +
          "&left=0"
      )
    }
  }

  test("prepareRequestUrl generates expected Url when given an event type") {
    new Fixture {
      val actor = trackerActor("http://www.legaltorrents.com:7070/announce")
      val result = actor.underlyingActor.prepareRequestUrl(Some(AnnounceEventStarted()))
      result should be(
        "http://www.legaltorrents.com:7070/announce?" +
          "info_hash=info+Hash%26Here&" +
          s"peer_id=peerIdHere&" +
          s"port=6881" +
          "&uploaded=0" +
          "&downloaded=0" +
          "&left=0" +
          "&event=started"
      )
    }
  }

  def successfulHttpResponseBody = "d8:intervali300e5:peerslee"

  def successfulHttpResponse = {
    HttpResponse(
      StatusCode.int2StatusCode(200),
      HttpEntity(successfulHttpResponseBody),
      Nil,
      HttpProtocols.`HTTP/1.1`
    )
  }

  def successfulTrackerResponseMsg = AvailablePeerSet(Set.empty[AvailablePeerDetails])

  def failedHttpResponse = {
    HttpResponse(
      StatusCode.int2StatusCode(404),
      HttpEntity("nope"),
      Nil,
      HttpProtocols.`HTTP/1.1`
    )
  }
}
