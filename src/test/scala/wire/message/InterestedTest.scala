package wire.message

import akka.util.ByteString

class InterestedTest extends SimpleMessageTest {
  def messageName = "Interested"
  def messageType = 2
  def messageParser(str: ByteString) = Interested(str)
  def messageFactory: Message = Interested()
}
