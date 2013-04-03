package torrent.peer

import spray.io.IOBridge.Connection
import spray.io.{ConnectionActors, IOServer}
import akka.actor.{Props, ActorRef}

trait PeerAccepterComponent {
  val peerAccepter: ActorRef
}

class PeerAccepterTcp extends IOServer with ConnectionActors {

  override def createConnectionActor(connection: Connection): ActorRef = {
    context.system.actorOf(Props(new InboundPeer(connection)))
  }

  override def nextConnectionActorName: String = {
    s"peer:inbound:${super.nextConnectionActorName}"
  }
}