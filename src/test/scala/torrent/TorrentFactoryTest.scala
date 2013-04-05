package torrent

import main.TestSystem
import akka.actor.ActorSystem

class TorrentFactoryTest(_system: ActorSystem) extends TestSystem(_system) {
  def this() = this(ActorSystem("TorrentFactoryTest"))

  override val torrentFactory = new TorrentFactory

  test("generatePeerId generates a string with proper prefix and length 20") {
    val peerId = torrentFactory.generatePeerId
    peerId should have length(20)
    peerId should startWith ("-SK0001-")
  }
}
