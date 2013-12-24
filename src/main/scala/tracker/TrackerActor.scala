package tracker

import akka.actor.{ActorRef, Actor}
import akka.pattern.{ask, pipe}
import akka.util.{ByteString, Timeout}
import bencoding.messages.TrackerResponse
import concurrent.ExecutionContext
import concurrent.duration._
import java.net.URL
import spray.http.HttpMethods._
import spray.http._
import tracker.TrackerActor.{TrackerAnnounceRequestMsg, TrackerAnnounceFailure, TrackerAnnounceResponseMsg}
import utils.Utils

class TrackerActor(httpIoManager: ActorRef) extends Actor {
  implicit val ec = ExecutionContext.global
  implicit val timeout: Timeout = 5 seconds span

  /* TODO: tracker actor should be responsible for its own counter and just
           send message to main torrent actor (parent) with updates */

  override def receive = {
    case TrackerAnnounceRequestMsg(r) => announceToTracker(sender, TrackerActor.prepareRequestUrl(r))
  }

  //TODO: follow real ask pattern (with recover) to handle all errors properly
  def announceToTracker(sender: ActorRef, url: String) {
    val httpResponseFuture = ask(httpIoManager, HttpRequest(GET, Uri(url))).mapTo[HttpResponse]

    httpResponseFuture.map((response: HttpResponse) => {
      if (response.status.isSuccess)
        TrackerAnnounceResponseMsg(TrackerResponse(ByteString(response.entity.asString)))
      else
        new TrackerAnnounceFailure(s"Non-successful response from tracker GET Request: $url")
    }).pipeTo(sender)
  }
}

object TrackerActor {

  class TrackerAnnounceFailure(msg: String) extends Exception(msg)

  //messages
  case class TrackerAnnounceResponseMsg(response: TrackerResponse)

  case class TrackerAnnounceRequestMsg(request: TrackerRequest)

  //convenience
  case class TrackerRequest(
    baseUrl: String,
    infoHash: ByteString,
    peerId: String,
    port: Int,
    uploaded: Int,
    downloaded: Int,
    left: Int,
    eventType: Option[AnnounceEvent] = None)

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