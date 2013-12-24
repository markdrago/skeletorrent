package tracker

import akka.actor.{Cancellable, ActorRef, Actor}
import akka.pattern.{ask, pipe}
import akka.util.{ByteString, Timeout}
import bencoding.messages.TrackerPeerDetails
import bencoding.messages.TrackerResponse
import concurrent.ExecutionContext
import concurrent.duration._
import java.net.URL
import scala.language.postfixOps
import spray.http.HttpMethods._
import spray.http._
import tracker.TrackerActor._
import utils.Utils

class TrackerActor(httpIoManager: ActorRef) extends Actor {
  implicit val ec = ExecutionContext.global
  implicit val timeout: Timeout = 5 seconds span

  //set only once after TrackerInit message
  var baseUrl = ""
  var infoHash = ByteString.empty
  var peerId = ""
  var port = 0

  //modified when parent (torrent) sends updated state message
  var uploaded = 0
  var downloaded = 0
  var left = 0

  //modified when tracker response received
  var requestInterval = 0
  var requestIntervalCancellable: Option[Cancellable] = None

  override def receive = pendingInitState

  def pendingInitState: Receive = {
    case m: TrackerInit => handleTrackerInit(m)
  }

  def steadyState: Receive = {
    case TrackerTimer     => announceToTracker(None)
    case m: TrackerStatus => handleTrackerStatus(m)
    case m: TrackerEvent  => announceToTracker(Some(m.eventType))
  }

  def handleTrackerInit(msg: TrackerInit) {
    //set initial state
    baseUrl = msg.baseUrl
    infoHash = msg.infoHash
    peerId = msg.peerId
    port = msg.port

    //send initial announce message to tracker
    announceToTracker(Some(AnnounceEventStarted()))

    //wait for results
    context.become(steadyState)
  }

  def handleTrackerStatus(msg: TrackerStatus) {
    uploaded = msg.uploaded
    downloaded = msg.downloaded
    left = msg.left
  }

  def announceToTracker(event: Option[AnnounceEvent]) {
    val url = prepareRequestUrl(event)
    val httpResponseFuture = (httpIoManager ? HttpRequest(GET, Uri(url))).mapTo[HttpResponse]

    httpResponseFuture.map((response: HttpResponse) => {
      if (response.status.isSuccess) {
        val parsedResponse = TrackerResponse(ByteString(response.entity.asString))
        updateTrackerInterval(parsedResponse.interval)
        TrackerPeerSet(parsedResponse.peers.toSet)
      }
      else
        TrackerFailure(s"Non-successful response from tracker GET Request: $url (${response.message})")
    }).recover {
      case ex => TrackerFailure(ex.getMessage)
    }.pipeTo(context.parent)
  }

  def prepareRequestUrl(event: Option[AnnounceEvent]): String = {
    var initialSep = "?"
    if (urlHasQueryPart(baseUrl))
      initialSep = "&"

    val buf = new StringBuilder(baseUrl)
    buf ++= s"${initialSep}info_hash=" + Utils.urlEncode(infoHash)
    buf ++= s"&peer_id=$peerId"
    buf ++= s"&port=$port"
    buf ++= s"&uploaded=$uploaded"
    buf ++= s"&downloaded=$downloaded"
    buf ++= s"&left=$left"
    for (e <- event) buf ++= s"&event=${e.name}"
    buf.toString()
  }

  private def urlHasQueryPart(url: String) = {
    val urlDetails = new URL(url)
    urlDetails.getQuery != null && !urlDetails.getQuery.isEmpty
  }

  def updateTrackerInterval(newInterval: Int) {
    //if we are not yet on a schedule or the schedule is changing
    if (requestIntervalCancellable.isEmpty || newInterval != requestInterval) {
      requestInterval = newInterval

      //cancel potentially pre-existing timer
      requestIntervalCancellable.foreach((cancellable) => cancellable.cancel())

      context.system.scheduler.schedule(requestInterval seconds, requestInterval seconds, self, TrackerTimer)
    }
  }
}

object TrackerActor {
  case class TrackerInit(baseUrl: String, infoHash: ByteString, peerId: String, port: Int)
  case class TrackerStatus(uploaded: Int, downloaded: Int, left: Int)
  case class TrackerEvent(eventType: AnnounceEvent)
  case class TrackerFailure(msg: String)
  case object TrackerTimer

  //TODO: move this message to Torrent (not Tracker) and change TrackerPeerDetails to AvailablePeerDetails
  case class TrackerPeerSet(peers: Set[TrackerPeerDetails])
}