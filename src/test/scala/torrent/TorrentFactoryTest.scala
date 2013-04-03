package torrent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import main.TestSystem
import akka.actor.ActorSystem
import akka.testkit.TestKit

class TorrentFactoryTest(_system: ActorSystem) extends TestKit(_system) with FunSuite with ShouldMatchers with TestSystem {
  //need to provide a no-argument constructor
  def this() = this(ActorSystem("TorrentFactoryTest"))

  //since we are making our own actor system, make sure we use the same one
  //within TestSystem by overriding testActorSystem here
  override def testActorSystem = _system

  override val torrentFactory = new TorrentFactory

  test("generatePeerId generates a string with proper prefix and length 20") {
    val peerId = torrentFactory.generatePeerId
    peerId should have length(20)
    peerId should startWith ("-SK0001-")
  }
}
