package wire.message

import akka.util.ByteString

class UnchokeTest extends SimpleMessageTest {
  def messageName = "Unchoke"
  def messageType = 1
  def messageParser(str: ByteString) = Unchoke(str)
  def messageFactory: Message = Unchoke()
}
