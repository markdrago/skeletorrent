package bencoding.messages

import bencoding.BDecoder
import akka.util.ByteString
import bencoding.items.{BEncodedString, BEncodedMap, BEncodedList, BEncodedInt}

case class TrackerPeerDetails(peerId: ByteString, ip: String, port: Int)
case class TrackerResponse(interval: Int, peers: List[TrackerPeerDetails])

//TODO: deduplicate some stuff b/w this and the similar MetaInfo object
object TrackerResponse {
  val bdecoder = new BDecoder

  def apply(bytes: ByteString): TrackerResponse = {
    val bencodedItem = bdecoder.decodeItem(bytes)

    bencodedItem match {
      case m:BEncodedMap => TrackerResponse(m)
      case _ => throw new IllegalArgumentException("TrackerResponse data must contain a top-level Map")
    }
  }

  def apply(dict: BEncodedMap): TrackerResponse = {
    TrackerResponseValidator.validate(dict)

    def interval = dict.get("interval").get.asInstanceOf[BEncodedInt].toInt

    def peers = {
      dict.get("peers").get.asInstanceOf[BEncodedList].collect({
        case map:BEncodedMap => {
          TrackerPeerDetails(
            map.get("peer id").get.asInstanceOf[BEncodedString].value,
            map.get("ip").get.toString,
            map.get("port").get.toInt
          )
        }
      }).toList
    }

    TrackerResponse(interval, peers)
  }
}