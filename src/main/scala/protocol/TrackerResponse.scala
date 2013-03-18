package protocol

import bencoding.{BDecoder, BEncodedList, BEncodedInt, BEncodedMap}
import akka.util.ByteString

class TrackerResponse(val dict: BEncodedMap) {
  TrackerResponseValidator.validate(dict)

  def interval = dict.get("interval").get.asInstanceOf[BEncodedInt].toInt

  def peers = {
    dict.get("peers").get.asInstanceOf[BEncodedList].collect({
      case map:BEncodedMap => {
        TrackerPeerDetails(
          map.get("peer id").get.toString,
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