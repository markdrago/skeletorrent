package bencoding.messages

import akka.util.ByteString
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import bencoding.{BEncoder, BDecoder}

//TODO: deal with failure response from tracker

class TrackerResponseTest extends FunSuite with ShouldMatchers {
  test("TrackerResponse constructor doesn't allow creation of invalid TrackerResponses") {
    val dict = (new BDecoder).decodeMap(ByteString("d1:a1:be"))
    evaluating { TrackerResponse(dict) } should produce [IllegalArgumentException]
  }

  test("TrackerResponse can produce interval") {
    getValidTrackerResponse.interval should  be (800)
  }

  test("TrackerResponse can produce list of peers") {
    val list = getValidTrackerResponse.peers
    list should have size 2
    list.head.peerId should be (ByteString("abcdefghijklmnopqrst"))
    list.head.ip should be ("1.2.3.4")
    list.head.port should be (6881)
    list.last.peerId should be (ByteString("12345678901234567890"))
    list.last.ip should be ("130.130.130.130")
    list.last.port should be (51415)
  }

  test("TrackerResponse equality works") {
    val tr1 = getValidTrackerResponse
    val tr2 = getValidTrackerResponse
    tr1.equals(tr2) should be (true)
    tr1.equals("abc") should be (false)
  }

  test("TrackerResponse can not be created with data that has a non-map top level element") {
    evaluating { TrackerResponse(ByteString("li1ee")) } should produce [IllegalArgumentException]
  }

  def getValidTrackerResponse = {
    getTrackerResponse(TrackerResponseSample.valid_response)
  }

  def getTrackerResponse(map: Map[String, Any]) = {
    TrackerResponse((new BEncoder).encodeMap(map))
  }
}