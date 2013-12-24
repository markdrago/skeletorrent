package tracker

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe, ImplicitSender, TestKit}
import akka.util.ByteString
import bencoding.messages.TrackerPeerDetails
import concurrent.duration._
import org.scalatest.{Matchers, FunSuiteLike}
import scala.Some
import scala.language.postfixOps
import spray.http._
import tracker.TrackerActor._
import utils.ParentChildMediator

class TrackerActorTest
  extends TestKit(ActorSystem("TrackerActorTest"))
  with ImplicitSender
  with FunSuiteLike
  with Matchers {

  val httpManagerProbe = TestProbe()

  private trait Fixture {
    val trackerActor = TestActorRef(new TrackerActor(httpManagerProbe.ref))
  }

  private trait MediatorFixture {
    val fosterParentProbe = TestProbe()
    val trackerActor = ParentChildMediator(fosterParentProbe.ref, Props(classOf[TrackerActor], httpManagerProbe.ref))
  }

  test("TrackerAnnouncer replies with TrackerAnnounceResponseMsg on success") {
    new MediatorFixture {
      trackerActor ! exampleTrackerInit()
      httpManagerProbe.expectMsgType[HttpRequest](1 second)
      httpManagerProbe.reply(successfulHttpResponse)
      fosterParentProbe.expectMsg(successfulTrackerResponseMsg)
    }
  }

  test("TrackerAnnouncer replies with TrackerFailure after failed HTTP response") {
    new MediatorFixture {
      trackerActor ! exampleTrackerInit()
      httpManagerProbe.expectMsgType[HttpRequest](1 second)
      httpManagerProbe.reply(failedHttpResponse)
      fosterParentProbe.expectMsgType[TrackerFailure]
    }
  }

  test("prepareRequestUrl generates expected Url in simple situation") {
    new Fixture {
      trackerActor ! exampleTrackerInit()
      val result = trackerActor.underlyingActor.prepareRequestUrl(None)
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
      trackerActor ! exampleTrackerInit("http://www.legaltorrents.com:7070/announce?key=1")
      val result = trackerActor.underlyingActor.prepareRequestUrl(None)
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
      trackerActor ! exampleTrackerInit("http://www.legaltorrents.com:7070/announce")
      val result = trackerActor.underlyingActor.prepareRequestUrl(Some(AnnounceEventStarted()))
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

  def exampleTrackerInit(
    baseUrl: String = "http://www.legaltorrents.com:7070/announce",
    eventType: Option[AnnounceEvent] = None) = {

    TrackerInit(
      baseUrl,
      ByteString("info Hash&Here"),
      "peerIdHere",
      6881
    )
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

  def successfulTrackerResponseMsg = TrackerPeerSet(Set.empty[TrackerPeerDetails])

  def failedHttpResponse = {
    HttpResponse(
      StatusCode.int2StatusCode(404),
      HttpEntity("nope"),
      Nil,
      HttpProtocols.`HTTP/1.1`
    )
  }
}
