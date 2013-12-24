package torrent.peer

import akka.actor.ActorRef
import akka.util.ByteString

//TODO: kill this whole concept of tags left over from old spray io
case class TorrentStateTag(
  torrent: ActorRef,
  infoHash: ByteString,
  peerId: String
)