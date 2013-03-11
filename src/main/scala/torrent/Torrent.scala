package torrent

import akka.actor.Actor
import util.Random
import akka.util.Timeout
import protocol.MetaInfo
import concurrent.duration._
import concurrent.ExecutionContext
import utils.Utils
import java.net.URL
import tracker.{TrackerAnnouncementMsg, AnnounceEvent}

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

  def announceToTracker {
    val announcer = context.actorFor(context.system / "tracker-announcer")
    announcer ! TrackerAnnouncementMsg(trackerGetRequestUrl())
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
