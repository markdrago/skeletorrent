package wire.message

import akka.util.ByteString

case class NotInterested() extends SimpleMessage {
  val serialize: ByteString = serializeWithMessageType(3)
}

object NotInterested extends MessageParser with SimpleMessageValidator {
  def apply(str: ByteString): NotInterested = {
    checkValidForMessageType(str, 3, "NotInterested")
    NotInterested()
  }
}