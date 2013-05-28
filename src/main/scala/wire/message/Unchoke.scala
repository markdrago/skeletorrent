package wire.message

import akka.util.ByteString

case class Unchoke() extends SimpleMessage {
  val serialize: ByteString = serializeWithMessageType(1)
}

object Unchoke extends MessageParser with SimpleMessageValidator {
  def apply(str: ByteString): Unchoke = {
    checkValidForMessageType(str, 1, "Unchoke")
    Unchoke()
  }
}