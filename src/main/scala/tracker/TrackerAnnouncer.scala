package tracker

import akka.actor.{ActorRef, Actor}
import akka.pattern.{ask, pipe}
import spray.httpx.RequestBuilding._
import spray.http.HttpResponse
import concurrent.ExecutionContext
import akka.util.{ByteString, Timeout}
import concurrent.duration._
import bencoding.messages.TrackerResponse

case class TrackerAnnounceRequestMsg(url: String)
case class TrackerAnnounceResponseMsg(response: TrackerResponse)
class TrackerAnnounceFailure(msg: String) extends Exception(msg)

trait TrackerAnnouncerComponent {
  this: HttpClientComponent =>
  val trackerAnnouncer: ActorRef

  class TrackerAnnouncer extends Actor {
    implicit val ec = ExecutionContext.global
    implicit val timeout: Timeout = 5 seconds span

    override def receive = {
      case TrackerAnnounceRequestMsg(u) => announceToTracker(u)
    }

    def announceToTracker(url: String) {
      ask(httpClient, Get(url))
        .mapTo[HttpResponse]
        .map {
          response =>
            if (response.status.isSuccess)
              TrackerAnnounceResponseMsg(
                TrackerResponse(ByteString(response.entity.buffer))
              )
            else
              new TrackerAnnounceFailure(s"Non-successful response from tracker GET Request: $url")
        }
        .pipeTo(sender)
    }
  }
}