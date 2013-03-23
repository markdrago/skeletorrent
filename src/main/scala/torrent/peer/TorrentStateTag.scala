package torrent.peer

import akka.actor.ActorRef
import akka.util.ByteString

case class TorrentStateTag(
  torrent: ActorRef,
  infoHash: ByteString,
  peerId: String
)