package wire.message

import akka.util.ByteString

case class Interested() extends SimpleMessage {
  val serialize: ByteString = serializeWithMessageType(2)
}

object Interested extends MessageParser with SimpleMessageValidator {
  def apply(str: ByteString): Interested = {
    checkValidForMessageType(str, 2, "Interested")
    Interested()
  }
}