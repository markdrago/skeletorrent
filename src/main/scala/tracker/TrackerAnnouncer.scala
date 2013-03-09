package tracker

import akka.actor.{ActorRef, Actor}
import akka.pattern.{ask, pipe}
import spray.httpx.RequestBuilding._
import spray.http.HttpResponse
import torrent.AnnounceResponseMsg
import concurrent.ExecutionContext
import akka.util.Timeout
import concurrent.duration._

case class TrackerAnnouncementMsg(val url: String)

trait TrackerAnnouncerComponent {
  this: HttpClientComponent =>
  val trackerAnnouncer: ActorRef

  class TrackerAnnouncer extends Actor {
    implicit val ec = ExecutionContext.global
    implicit val timeout: Timeout = 5 seconds span

    def receive = {
      case TrackerAnnouncementMsg(u) => announceToTracker(u)
    }

    def announceToTracker(url: String) {
      ask(httpClient, Get(url))
        .mapTo[HttpResponse]
        .map {
          response =>
            if (response.status.isSuccess)
              AnnounceResponseMsg(response.entity.asString)
            else
              //TODO: create a new type of exception or something
              throw new RuntimeException(s"Non-successful response from tracker GET Request: $url")
        }
        .pipeTo(sender)
    }
  }
}