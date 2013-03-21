package torrent

import spray.io.{ConnectionActors, IOExtension, IOServer}
import spray.io.IOBridge.Connection
import akka.actor.{Props, ActorRef}

trait TorrentListenerComponent {
  val torrentListener: TorrentListener
  class TorrentListener extends IOServer with ConnectionActors {
    val ioBridge = IOExtension(context.system).ioBridge()

    def createConnectionActor(connection: Connection): ActorRef = {
      context.system.actorOf(Props(new TorrentPeer))
    }
  }
}