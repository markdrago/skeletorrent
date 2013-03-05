package torrent

import akka.actor.Actor
import akka.pattern.ask
import util.{Random, Success, Failure}
import akka.util.Timeout
import metainfo.MetaInfo
import spray.http.HttpResponse
import spray.httpx.RequestBuilding._
import concurrent.duration._
import concurrent.ExecutionContext
import scala.Predef.{String, println}
import utils.Utils
import java.net.URL
import tracker.AnnounceEvent

case class InjectMetainfoFileMsg(filename: String)
case class AnnounceResponseMsg(response: String)

class Torrent extends Actor {
  implicit val ec = ExecutionContext.global
  implicit val timeout: Timeout = 5 seconds span

  var metainfo: MetaInfo = null
  val peerId = Torrent.generatePeerId
  val port = 6881
  var uploaded = 0
  var downloaded = 0
  var left = 0

  def receive = {
    case InjectMetainfoFileMsg(f) => initMetaInfoFile(f)
    case AnnounceResponseMsg(s) => println(s)
  }

  def initMetaInfoFile(metainfoFileName: String) {
    this.metainfo = MetaInfo(Utils.readFile(metainfoFileName))
    println(Utils.urlEncode(metainfo.infoHash))
    announceToTracker
  }

  //TODO: pull tracker operations out in to a separate actor
  def announceToTracker {
    val httpClient = context.actorFor(context.system / "http-client")

    val trackerUrl = trackerGetRequestUrl()
    val responseFuture = ask(httpClient, Get(trackerUrl)).mapTo[HttpResponse]
    responseFuture onComplete {
      case Success(response) => {
        if (response.status.isSuccess) {
          self ! AnnounceResponseMsg(response.entity.asString)
        } else {
          println(s"Got a non-successful response from tracker GET request to: $trackerUrl")
        }
      }
      case Failure(error) => {
        println(s"Unable to make tracker GET request to: $trackerUrl")
        error.printStackTrace()
      }
    }
  }

  def trackerGetRequestUrl(eventType: Option[AnnounceEvent] = None): String = {
    val urlDetails = new URL(metainfo.trackerUrl)

    var initialSep = "?"
    if (urlDetails.getQuery != null && !urlDetails.getQuery.isEmpty)
      initialSep = "&"

    val buf = new StringBuilder
    buf ++= metainfo.trackerUrl
    buf ++= s"${initialSep}info_hash=" + Utils.urlEncode(metainfo.infoHash)
    buf ++= s"&peer_id=$peerId"
    buf ++= s"&port=$port"
    buf ++= s"&uploaded=$uploaded"
    buf ++= s"&downloaded=$downloaded"
    buf ++= s"&left=$left"
    for (e <- eventType) buf ++= s"&event=${e.name}"
    buf.toString
  }
}

object Torrent {
  def generatePeerId: String = {
    val prefix = "-SK0001-"
    prefix + new String((new Random()).alphanumeric.take(20 - prefix.length).toArray)
  }
}
