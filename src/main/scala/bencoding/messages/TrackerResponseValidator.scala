package bencoding.messages

import bencoding.items.{BEncodedMap, BEncodedList, BEncodedInt, BEncodedString}

object TrackerResponseValidator extends Validator {
  def validate(dict: BEncodedMap) = {
    checkRequiredElement(dict, "interval", "TrackerResponse", classOf[BEncodedInt])
    checkRequiredElement(dict, "peers", "TrackerResponse", classOf[BEncodedList])
    dict.get("peers").get.asInstanceOf[BEncodedList].map({
      case m:BEncodedMap => validatePeer(m)
      case _ => throw new IllegalArgumentException("A peer in TrackerResponse was not a dictionary")
    })
  }

  private def validatePeer(peer: BEncodedMap) {
    validatePeerId(peer)
    validatePort(peer)
    validateIpAddress(peer)
  }

  private def validatePeerId(peer: BEncodedMap) {
    checkRequiredElement(peer, "peer id", "TrackerResponse/peer[x]/peer id", classOf[BEncodedString])
    val peerId = peer.get("peer id").get.asInstanceOf[BEncodedString].value
    require(peerId.length <= 20, s"TrackerResponse/peer[x]/peer id was too long: $peerId")
  }

  private def validatePort(peer: BEncodedMap) {
    checkRequiredElement(peer, "port", "TrackerResponse/peer[x]/port", classOf[BEncodedInt])
    val port = peer.get("port").get.asInstanceOf[BEncodedInt].toInt
    require(isValidPortNumber(port), s"TrackerResponse/peer[x]/port is not a valid port: $port")
  }

  private def isValidPortNumber(port: Int): Boolean = {
    (1 to 65535) contains port
  }

  private def validateIpAddress(peer: BEncodedMap) {
    checkRequiredElement(peer, "ip", "TrackerResponse/peer[x]/ip", classOf[BEncodedString])
    val ip = peer.get("ip").get.asInstanceOf[BEncodedString].toString
    require(isValidIpAddress(ip), s"TrackerResponse/peer[x]/ip is not a valid ip address: $ip")
  }

  private def isValidIpAddress(ip: String): Boolean = {
    ipIsNumericWithDots(ip) &&
    ipContainsFourSections(ip) &&
    ipSectionsAreAllValid(ip)
  }

  private def ipIsNumericWithDots(ip: String): Boolean = {
    ip.matches("[0-9\\.]+")
  }

  private def ipContainsFourSections(ip: String): Boolean = {
    ipSections(ip).length == 4
  }

  private def ipSectionsAreAllValid(ip: String): Boolean = {
    ipSections(ip).map(_.toInt).foldLeft(true)((acc, piece) => {acc && ((0 to 255) contains piece)})
  }

  private def ipSections(ip: String): Seq[String] = {
    ip.split('.').filter(_.length > 0)
  }
}
