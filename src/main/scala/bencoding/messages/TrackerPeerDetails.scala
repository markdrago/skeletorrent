package bencoding.messages

import akka.util.ByteString

case class TrackerPeerDetails(peerId: ByteString, ip: String, port: Int)
