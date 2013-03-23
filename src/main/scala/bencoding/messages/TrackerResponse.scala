package bencoding.messages

import bencoding.BDecoder
import akka.util.ByteString
import bencoding.items.{BEncodedString, BEncodedMap, BEncodedList, BEncodedInt}

class TrackerResponse(val dict: BEncodedMap) {
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
    })
  }

  override def equals(other: Any): Boolean = {
    other match {
      case t:TrackerResponse => this.dict == t.dict
      case _ => false
    }
  }
}

//TODO: deduplicate this and the very similar MetaInfo object
object TrackerResponse {
  val bdecoder = new BDecoder

  def apply(bytes: ByteString): TrackerResponse = {
    val bencodedItem = bdecoder.decodeItem(bytes)

    bencodedItem match {
      case m:BEncodedMap => new TrackerResponse(m)
      case _ => throw new IllegalArgumentException("TrackerResponse data must contain a top-level Map")
    }
  }
}