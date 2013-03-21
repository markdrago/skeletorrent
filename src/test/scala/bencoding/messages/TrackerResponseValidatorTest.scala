package bencoding.messages

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import bencoding.{BDecoder, BEncoder}

class TrackerResponseValidatorTest extends FunSuite with ShouldMatchers {
  val bencoder = new BEncoder
  val bdecoder = new BDecoder

  test("checkValidity throws when interval is not present") {
    val map = TrackerResponseSample.valid_response - "interval"
    checkValidityForFailure(map, "interval")
  }

  test("checkValidity throws when interval is not an integer") {
    val map = (TrackerResponseSample.valid_response - "interval") + ("interval" -> "hello")
    checkValidityForFailure(map, "interval")
  }

  test("checkValidity throws when peers is not present") {
    val map = TrackerResponseSample.valid_response - "peers"
    checkValidityForFailure(map, "peers")
  }

  test("checkValidity throws when peers is not a list") {
    val map = (TrackerResponseSample.valid_response - "peers") + ("peers" -> "hello")
    checkValidityForFailure(map, "peers")
  }

  test("checkValidity is okay with an empty peers list") {
    val map = (TrackerResponseSample.valid_response - "peers") + ("peers" -> List())
    val dict = (new BEncoder).encodeMap(map)
    TrackerResponseValidator.validate(dict)
  }

  test("checkValidity throws if an item in peers is not a dictionary") {
    val map = (TrackerResponseSample.valid_response - "peers") + ("peers" -> List("hello"))
    checkValidityForFailure(map, "peer")
  }

  //checks for peers
  def validPeer = Map("peer id" -> "abc", "ip" -> "1.2.3.4", "port" -> 6881)
  def peerWithPort(port: Any) = (validPeer - "port") + ("port" -> port)
  def peerWithIp(ip: Any) = (validPeer - "ip") + ("ip" -> ip)
  def peerWithPeerId(id: Any) = (validPeer - "peer id") + ("peer id" -> id)

  //port numbers
  test("checkValidity throws if a peer does not have a port number") {
    checkValidityForFailureWithPeer(validPeer - "port", "port")
  }

  test("checkValidity throws if a peer has a non-integer port") {
    checkValidityForFailureWithPeer(peerWithPort("hello"), "port")
  }

  test("checkValidity throws if a peer has a negative port") {
    checkValidityForFailureWithPeer(peerWithPort(-6881), "port")
  }

  test("checkValidity throws if a peer has a port which exceeds the limit") {
    checkValidityForFailureWithPeer(peerWithPort(65536), "port")
  }

  //ip addresses
  test("checkValidity throws if a peer does not have an ip address") {
    checkValidityForFailureWithPeer(validPeer - "ip", "ip")
  }

  test("checkValidity throws if a peer has a non-string ip address") {
    checkValidityForFailureWithPeer(peerWithIp(1234), "ip")
  }

  test("checkValidity throws if a peer has an ip with a letter") {
    checkValidityForFailureWithPeer(peerWithIp("a1.2.3.4"), "ip")
  }

  test("checkValidity throws if a peer has an ip with too few sections") {
    checkValidityForFailureWithPeer(peerWithIp("1.2.3"), "ip")
  }

  test("checkValidity throws if a peer has an ip with too many sections") {
    checkValidityForFailureWithPeer(peerWithIp("1.2.3.4.5"), "ip")
  }

  test("checkValidity throws if a peer has an ip with a section that is > 255") {
    checkValidityForFailureWithPeer(peerWithIp("1.2.3.256"), "ip")
  }

  test("checkValidity throws if a peer has an ip with a section that is empty") {
    checkValidityForFailureWithPeer(peerWithIp("1.2..4"), "ip")
  }

  //peer id
  test("checkValidity throws if a peer id is not present") {
    checkValidityForFailureWithPeer(validPeer - "peer id", "peer id")
  }

  test("checkValidity throws if a peer id is not a string") {
    checkValidityForFailureWithPeer(peerWithPeerId(123), "peer id")
  }

  test("checkValidity throws if a peer id is too long") {
    checkValidityForFailureWithPeer(peerWithPeerId("a" * 21), "peer id")
  }

  //valid
  test("checkValidity is cool with a valid response") {
    TrackerResponseValidator.validate(bencoder.encodeMap(TrackerResponseSample.valid_response))
  }

  test("checkValidity is cool with a valid real world response") {
    TrackerResponseValidator.validate(bdecoder.decodeMap(TrackerResponseSample.valid_real_world_response))
  }

  def addPeer(input: Map[String, Any], peerToAdd: Map[String, Any]): Map[String, Any] = {
    val peerList = input.get("peers").get.asInstanceOf[List[_]]
    (input - "peers") + ("peers" -> (peerToAdd :: peerList))
  }

  def checkValidityForFailureWithPeer(peer: Map[String, Any], expectedMissing: String) {
    checkValidityForFailure(addPeer(TrackerResponseSample.valid_response, peer), expectedMissing)
  }

  def checkValidityForFailure(input: Map[String, Any], expectedMissing: String) {
    val dict = bencoder.encodeMap(input)
    val caught = evaluating { TrackerResponseValidator.validate(dict) } should produce [IllegalArgumentException]
    caught.getMessage should include (expectedMissing)
  }
}
