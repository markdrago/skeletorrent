package torrent

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.{TestKit, TestActorRef}
import akka.actor.{Props, ActorSystem}
import bencoding.messages.{MetaInfo, MetaInfoSample}
import main.TestSystem

class TorrentTest(_system: ActorSystem) extends TestKit(_system) with TestSystem
    with FunSuite with ShouldMatchers with BeforeAndAfter {

  //need to provide a no-argument constructor
  def this() = this(ActorSystem("TorrentTest"))

  //since we are making our own actor system, make sure we use the same one
  //within TestSystem by overriding testActorSystem here
  override def testActorSystem = _system

  var t: Torrent = null

  before {
    val actorRef = TestActorRef(Props(
      new Torrent(
        6881,
        "abc",
        MetaInfo(MetaInfoSample.get_metainfo_file_contents),
        peerAccepter,
        trackerAnnouncer
      )
    ))
    t = actorRef.underlyingActor
  }

  test("torrent sends binding message to peerAccepterProbe after receiving init message") {
    //TODO: move init file reading out of torrent so this can be more easily tested
    //TODO: actually test that message gets sent
    //t.receive should be (t.uninitializedState)
    //above may not actually be possible, perhaps test partial function definition?
  }



}
