package torrent.peer

sealed trait PeerType
case object PeerTypeOutbound extends PeerType
case object PeerTypeInbound extends PeerType
