package torrent

import spray.io.{ConnectionActors, IOServer}
import spray.io.IOBridge.{Bound, Connection}
import akka.actor.{Props, ActorRef}

class TorrentListenerTcp(torrent: ActorRef) extends IOServer with ConnectionActors {

  override def createConnectionActor(connection: Connection): ActorRef = {
    context.system.actorOf(Props(new TorrentPeer(torrent, connection)), nextConnectionActorName)
  }

  override def nextConnectionActorName: String = {
    s"peer:inbound:${super.nextConnectionActorName}"
  }
}