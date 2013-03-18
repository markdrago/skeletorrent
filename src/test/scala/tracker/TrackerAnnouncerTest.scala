package tracker

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import akka.actor.{Props, ActorSystem}
import akka.testkit._
import spray.http._
import spray.http.HttpResponse
import concurrent.duration._
import protocol.TrackerResponse
import akka.util.ByteString

class TrackerAnnouncerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with FunSuite with ShouldMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("TrackerAnnouncerTest"))

  override def afterAll() { system.shutdown() }

  val httpClientProbe = TestProbe()
  object TestSystem extends HttpClientComponent with TrackerAnnouncerComponent {
    override val httpClient = httpClientProbe.ref
    override val trackerAnnouncer = system.actorOf(Props(new TrackerAnnouncer))
  }

  test("TrackerAnnouncer replies with TrackerAnnounceResponseMsg on success") {
    TestSystem.trackerAnnouncer ! TrackerAnnounceRequestMsg("http://example.com")
    httpClientProbe.expectMsgType[HttpRequest](1000.millis)
    httpClientProbe.reply(successfulHttpResponse)
    expectMsg(successfulTrackerAnnounceResponseMsg)
  }

  test("TrackerAnnouncer replies with TrackerAnnounceFailure after failed HTTP response") {
    TestSystem.trackerAnnouncer ! TrackerAnnounceRequestMsg("http://example.com")
    httpClientProbe.expectMsgType[HttpRequest](1000.millis)
    httpClientProbe.reply(failedHttpResponse)
    expectMsgType[TrackerAnnounceFailure]
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
