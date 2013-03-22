package torrent

import akka.actor.{Props, Actor}
import util.Random
import akka.util.Timeout
import concurrent.duration._
import concurrent.ExecutionContext
import utils.Utils
import java.net.{InetSocketAddress, URL}
import tracker.{TrackerAnnounceResponseMsg, TrackerAnnounceRequestMsg, AnnounceEvent}
import torrent.Torrent.TorrentInitializationMsg
import bencoding.messages.{TrackerResponse, MetaInfo}
import spray.io.IOBridge.Bind
import spray.io.IOServer.Bound

class Torrent(val port: Int) extends Actor {
  implicit val ec = ExecutionContext.global
  implicit val timeout: Timeout = 5 seconds span

  val torrentListener = context.system.actorOf(Props(new TorrentListenerTcp(self)))

  var metainfo: MetaInfo = null
  val peerId = Torrent.generatePeerId
  var uploaded = 0
  var downloaded = 0
  var left = 0

  //TODO maybe use of become could clean up these unrelated receive handlers (check IOServer src)
  def receive = {
    case TorrentInitializationMsg(f) => initializeTorrentPhase1(f)
    case Bound(endpoint, tag) => announceToTracker()
    case TrackerAnnounceResponseMsg(resp) => handleTrackerResponse(resp)
  }

  private def initializeTorrentPhase1(metainfoFileName: String) {
    this.metainfo = MetaInfo(Utils.readFile(metainfoFileName))
    bindToListeningPort()
  }

  private def bindToListeningPort() {
    torrentListener ! new Bind(new InetSocketAddress(port), 100, ())
  }

  private def announceToTracker() {
    val announcer = context.actorFor(context.system / "tracker-announcer")
    announcer ! TrackerAnnounceRequestMsg(trackerGetRequestUrl())
  }

  private[torrent] def trackerGetRequestUrl(eventType: Option[AnnounceEvent] = None): String = {
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
    buf.toString()
  }

  private def handleTrackerResponse(resp: TrackerResponse) {
    resp.peers.foreach((peer) => {
      println(s"id: ${peer.peerId}, ip: ${peer.ip}, port: ${peer.port}")
    })
  }
}

object Torrent {
  def generatePeerId: String = {
    val prefix = "-SK0001-"
    prefix + new String((new Random()).alphanumeric.take(20 - prefix.length).toArray)
  }

  //messages
  case class TorrentInitializationMsg(filename: String)
}
