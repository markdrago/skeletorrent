package wire.message

import akka.util.ByteString

trait Message {
  def serialize: ByteString
}

trait MessageParser {
  def apply(str: ByteString): Message
}
