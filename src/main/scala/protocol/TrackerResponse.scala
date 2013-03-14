package protocol

import bencoding.{BEncodedList, BEncodedInt, BEncodedMap}

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
}
