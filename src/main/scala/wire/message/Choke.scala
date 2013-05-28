package wire.message

import akka.util.ByteString

case class Choke() extends SimpleMessage {
  val serialize: ByteString = serializeWithMessageType(0)
}

object Choke extends MessageParser with SimpleMessageValidator {
  def apply(str: ByteString): Choke = {
    checkValidForMessageType(str, 0, "Choke")
    Choke()
  }
}