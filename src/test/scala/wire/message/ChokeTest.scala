package wire.message

import akka.util.ByteString

class ChokeTest extends SimpleMessageTest {
  def messageName = "Choke"
  def messageType = 0
  def messageParser(str: ByteString) = Choke(str)
  def messageFactory: Message = Choke()
}
