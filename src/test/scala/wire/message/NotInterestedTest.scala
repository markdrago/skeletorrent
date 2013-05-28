package wire.message

import akka.util.ByteString

class NotInterestedTest extends SimpleMessageTest {
  def messageName = "NotInterested"
  def messageType = 3
  def messageParser(str: ByteString) = NotInterested(str)
  def messageFactory: Message = NotInterested()
}
