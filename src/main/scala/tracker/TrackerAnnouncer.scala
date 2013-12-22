package tracker

import akka.io.IO
import spray.can.Http
import spray.http._
import HttpMethods._
import akka.actor.{ActorRef, Actor}
import akka.pattern.{ask, pipe}
import spray.http.HttpResponse
import concurrent.ExecutionContext
import akka.util.{ByteString, Timeout}
import concurrent.duration._
import bencoding.messages.TrackerResponse
import tracker.TrackerAnnouncer.{TrackerAnnounceRequestMsg, TrackerAnnounceFailure, TrackerAnnounceResponseMsg}
import java.net.URL
import utils.Utils

trait TrackerAnnouncerComponent {
  this: HttpClientComponent =>
  val trackerAnnouncer: ActorRef

  class TrackerAnnouncer extends Actor {
    implicit val ec = ExecutionContext.global
    implicit val timeout: Timeout = 5 seconds span

    override def receive = {
      case TrackerAnnounceRequestMsg(r) => announceToTracker(sender, TrackerAnnouncer.prepareRequestUrl(r))
    }

    def announceToTracker(sender: ActorRef, url: String) {
      ask(httpClient, HttpRequest(GET, Uri(url)))
        .mapTo[HttpResponse]
        .map {
          response =>
            if (response.status.isSuccess)
              TrackerAnnounceResponseMsg(
                TrackerResponse(ByteString(response.entity.asString))
              )
            else
              new TrackerAnnounceFailure(s"Non-successful response from tracker GET Request: $url")
        }
        .pipeTo(sender)
    }
  }
}

object TrackerAnnouncer {
  class TrackerAnnounceFailure(msg: String) extends Exception(msg)

  //messages
  case class TrackerAnnounceResponseMsg(response: TrackerResponse)
  case class TrackerAnnounceRequestMsg(request: TrackerRequest)

  //convenience
  case class TrackerRequest (
    baseUrl: String,
    infoHash: ByteString,
    peerId: String,
    port: Int,
    uploaded: Int,
    downloaded: Int,
    left: Int,
    eventType: Option[AnnounceEvent] = None
  )

  def prepareRequestUrl(req: TrackerRequest): String = {
    var initialSep = "?"
    if (urlHasQueryPart(req.baseUrl))
      initialSep = "&"

    val buf = new StringBuilder
    buf ++= req.baseUrl
    buf ++= s"${initialSep}info_hash=" + Utils.urlEncode(req.infoHash)
    buf ++= s"&peer_id=${req.peerId}"
    buf ++= s"&port=${req.port}"
    buf ++= s"&uploaded=${req.uploaded}"
    buf ++= s"&downloaded=${req.downloaded}"
    buf ++= s"&left=${req.left}"
    for (e <- req.eventType) buf ++= s"&event=${e.name}"
    buf.toString()
  }

  private def urlHasQueryPart(url: String): Boolean = {
    val urlDetails = new URL(url)
    urlDetails.getQuery != null && !urlDetails.getQuery.isEmpty
  }
}