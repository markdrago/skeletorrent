package wire.message

import akka.util.ByteString

//TODO: add tests for Choke, Choke comp. object, and thereby SimpleMessage & SimpleMessageValidator
case class Choke() extends SimpleMessage {
  val serialize: ByteString = serializeWithMessageType(0)
}

object Choke extends MessageParser with SimpleMessageValidator {
  def apply(str: ByteString): Choke = {
    checkValidForMessageType(str, 0.toByte, "Choke")
    Choke()
  }
}