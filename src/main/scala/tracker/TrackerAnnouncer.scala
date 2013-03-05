package tracker

import akka.actor.Actor
import akka.pattern.ask
import spray.httpx.RequestBuilding._
import spray.http.HttpResponse
import util.{Failure, Success}
import torrent.AnnounceResponseMsg
import concurrent.ExecutionContext
import akka.util.Timeout
import concurrent.duration._

case class TrackerAnnouncementMsg(val url: String)

class TrackerAnnouncer extends Actor {
  implicit val ec = ExecutionContext.global
  implicit val timeout: Timeout = 5 seconds span

  def receive = {
    case TrackerAnnouncementMsg(u) => announceToTracker(u)
  }

  def announceToTracker(url: String) {
    val httpClient = context.actorFor(context.system / "http-client")

    val responseFuture = ask(httpClient, Get(url)).mapTo[HttpResponse]
    responseFuture onComplete {
      case Success(response) => {
        if (response.status.isSuccess) {
          sender ! AnnounceResponseMsg(response.entity.asString)
        } else {
          println(s"Got a non-successful response from tracker GET request to: $url")
        }
      }
      case Failure(error) => {
        println(s"Unable to make tracker GET request to: $url")
        error.printStackTrace()
      }
    }
  }
}
