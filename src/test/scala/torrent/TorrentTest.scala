package torrent

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.TestActorRef
import akka.actor.{Props, ActorSystem}
import bencoding.BEncoder
import tracker.AnnounceEventStarted
import bencoding.messages.{MetaInfoSample, MetaInfo}

class TorrentTest extends FunSuite with ShouldMatchers with BeforeAndAfter {
  implicit val system = ActorSystem("TorrentTest")
  var t: Torrent = null

  before {
    val actorRef = TestActorRef(Props(new Torrent(6881)))
    t = actorRef.underlyingActor
  }

  test("trackerGetRequestUrl generates expected Url in simple situation") {
    t.metainfo = MetaInfo(MetaInfoSample.get_metainfo_file_contents)
    val result = t.trackerGetRequestUrl()
    result should be (
      "http://www.legaltorrents.com:7070/announce?" +
      "info_hash=%CAj%C4%BB%D9q%D3%90%295%DB%CF%C2%D3%EA%25%B4%28%A5G&" +
      s"peer_id=${t.peerId}&" +
      s"port=${t.port}" +
      "&uploaded=0" +
      "&downloaded=0" +
      "&left=0"
    )
  }

  test("trackerGetRequestUrl generates expected Url if announce URL already has parameters") {
    var m = MetaInfoSample.get_metainfo_map_for_single_file
    m = (m - "announce") + ("announce" -> "http://www.legaltorrents.com:7070/announce?key=1")
    t.metainfo = new MetaInfo((new BEncoder).encodeMap(m))
    val result = t.trackerGetRequestUrl()
    result should be (
      "http://www.legaltorrents.com:7070/announce?key=1" +
        "&info_hash=%CAj%C4%BB%D9q%D3%90%295%DB%CF%C2%D3%EA%25%B4%28%A5G&" +
        s"peer_id=${t.peerId}&" +
        s"port=${t.port}" +
        "&uploaded=0" +
        "&downloaded=0" +
        "&left=0"
    )
  }

  test("trackerGetRequestUrl generates expected Url when given an event type") {
    t.metainfo = MetaInfo(MetaInfoSample.get_metainfo_file_contents)
    val result = t.trackerGetRequestUrl(Some(AnnounceEventStarted()))
    result should be (
      "http://www.legaltorrents.com:7070/announce?" +
        "info_hash=%CAj%C4%BB%D9q%D3%90%295%DB%CF%C2%D3%EA%25%B4%28%A5G&" +
        s"peer_id=${t.peerId}&" +
        s"port=${t.port}" +
        "&uploaded=0" +
        "&downloaded=0" +
        "&left=0" +
        "&event=started"
    )
  }

  test("generatePeerId generates a string with proper prefix and length 20") {
    val peerId = Torrent.generatePeerId
    peerId should have length(20)
    peerId should startWith ("-SK0001-")
  }
}
