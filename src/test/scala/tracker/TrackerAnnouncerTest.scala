package tracker

import akka.actor.{Props, ActorSystem}
import spray.http._
import spray.http.HttpResponse
import concurrent.duration._
import akka.util.ByteString
import bencoding.messages.TrackerResponse
import tracker.TrackerAnnouncer.{TrackerAnnounceResponseMsg, TrackerAnnounceFailure, TrackerAnnounceRequestMsg, TrackerRequest}
import main.TestSystem

class TrackerAnnouncerTest(_system: ActorSystem) extends TestSystem(_system) {
  def this() = this(ActorSystem("TrackerAnnouncerTest"))

  //replace the mock trackerAnnouncer in TestSystem with the real deal
  override val trackerAnnouncer = _system.actorOf(Props(new TrackerAnnouncer))

  test("TrackerAnnouncer replies with TrackerAnnounceResponseMsg on success") {
    trackerAnnouncer ! TrackerAnnounceRequestMsg(exampleTrackerRequest())
    httpClientProbe.expectMsgType[HttpRequest](1000.millis)
    httpClientProbe.reply(successfulHttpResponse)
    expectMsg(successfulTrackerAnnounceResponseMsg)
  }

  test("TrackerAnnouncer replies with TrackerAnnounceFailure after failed HTTP response") {
    trackerAnnouncer ! TrackerAnnounceRequestMsg(exampleTrackerRequest())
    httpClientProbe.expectMsgType[HttpRequest](1000.millis)
    httpClientProbe.reply(failedHttpResponse)
    expectMsgType[TrackerAnnounceFailure]
  }

  test("prepareRequestUrl generates expected Url in simple situation") {
    val result = TrackerAnnouncer.prepareRequestUrl(exampleTrackerRequest())
    result should be (
      "http://www.legaltorrents.com:7070/announce?" +
        "info_hash=info+Hash%26Here&" +
        s"peer_id=peerIdHere&" +
        s"port=6881" +
        "&uploaded=0" +
        "&downloaded=0" +
        "&left=0"
    )
  }

  test("prepareRequestUrl generates expected Url if announce URL already has parameters") {
    val result = TrackerAnnouncer.prepareRequestUrl(
      exampleTrackerRequest("http://www.legaltorrents.com:7070/announce?key=1")
    )
    result should be (
      "http://www.legaltorrents.com:7070/announce?key=1&" +
        "info_hash=info+Hash%26Here&" +
        s"peer_id=peerIdHere&" +
        s"port=6881" +
        "&uploaded=0" +
        "&downloaded=0" +
        "&left=0"
    )
  }

  test("prepareRequestUrl generates expected Url when given an event type") {
    val result = TrackerAnnouncer.prepareRequestUrl(exampleTrackerRequest(
      "http://www.legaltorrents.com:7070/announce",
      Some(AnnounceEventStarted())
    ))
    result should be (
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
  
  def exampleTrackerRequest(baseUrl: String = "http://www.legaltorrents.com:7070/announce",
                            eventType: Option[AnnounceEvent] = None) = {
    TrackerRequest(
      baseUrl,
      ByteString("info Hash&Here"),
      "peerIdHere",
      6881,
      0,
      0,
      0,
      eventType
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

  def successfulTrackerAnnounceResponseMsg = {
    TrackerAnnounceResponseMsg(
      TrackerResponse(ByteString(successfulHttpResponseBody))
    )
  }

  def failedHttpResponse = {
    HttpResponse(
      StatusCode.int2StatusCode(404),
      HttpEntity("nope"),
      Nil,
      HttpProtocols.`HTTP/1.1`
    )
  }
}
