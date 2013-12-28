package bencoding.messages

import akka.util.ByteString
import bencoding.BDecoder
import bencoding.items.{BEncodedString, BEncodedMap, BEncodedList, BEncodedInt}

case class AvailablePeerDetails(peerId: ByteString, host: String, port: Int)
case class TrackerResponse(interval: Int, peers: Set[AvailablePeerDetails])

//TODO: deduplicate some stuff b/w this and the similar MetaInfo object
object TrackerResponse {
  val bdecoder = new BDecoder

  def apply(bytes: ByteString): TrackerResponse = {
    val bencodedItem = bdecoder.decodeItem(bytes)

    bencodedItem match {
      case m: BEncodedMap => TrackerResponse(m)
      case _              => throw new IllegalArgumentException("TrackerResponse data must contain a top-level Map")
    }
  }

  def apply(dict: BEncodedMap): TrackerResponse = {
    TrackerResponseValidator.validate(dict)

    def interval = dict.get("interval").get.asInstanceOf[BEncodedInt].toInt

    def peers = {
      dict.get("peers").get.asInstanceOf[BEncodedList].collect({
        case map: BEncodedMap =>
          AvailablePeerDetails(
            map.get("peer id").get.asInstanceOf[BEncodedString].value,
            map.get("ip").get.toString,
            map.get("port").get.toInt
          )
      }).toSet
    }

    TrackerResponse(interval, peers)
  }
}