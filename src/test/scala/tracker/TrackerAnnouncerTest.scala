package tracker

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import akka.actor.{Props, ActorSystem}
import akka.testkit._
import spray.http._
import spray.http.HttpResponse
import concurrent.duration._
import torrent.AnnounceResponseMsg

class TrackerAnnouncerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with FunSuite with ShouldMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("TrackerAnnouncerTest"))

  override def afterAll = system.shutdown()

  val httpClientProbe = TestProbe()
  object TestSystem extends HttpClientComponent with TrackerAnnouncerComponent {
    override val httpClient = httpClientProbe.ref
    override val trackerAnnouncer = system.actorOf(Props(new TrackerAnnouncer))
  }

  def successfulHttpResponse = {
    HttpResponse(
      StatusCode.int2StatusCode(200),
      HttpEntity("result here"),
      Nil,
      HttpProtocols.`HTTP/1.1`
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

  test("TrackerAnnouncer replies with AnnounceResponseMsg on success") {
    TestSystem.trackerAnnouncer ! TrackerAnnouncementMsg("http://example.com")
    httpClientProbe.expectMsgType[HttpRequest](1000.millis)
    httpClientProbe.reply(successfulHttpResponse)
    expectMsg(AnnounceResponseMsg("result here"))
  }

  test("TrackerAnnouncer replies with TrackerAnnouncementFailure after failed HTTP response") {
    TestSystem.trackerAnnouncer ! TrackerAnnouncementMsg("http://example.com")
    httpClientProbe.expectMsgType[HttpRequest](1000.millis)
    httpClientProbe.reply(failedHttpResponse)
    expectMsgType[TrackerAnnouncementFailure]
  }
}
