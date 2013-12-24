package utils

import akka.actor.{Actor, Props}
import akka.testkit.TestProbe
import scala.language.implicitConversions

class ProbeForwarder(probe: TestProbe) extends Actor {
  override def receive = {
    case x => probe.ref forward x
  }
}

class ProbeForwarderProps(probe: TestProbe) {
  def props = Props(classOf[ProbeForwarder], probe)
}

object ProbeForwarderProps {
  implicit def probe2Props(probe: TestProbe): ProbeForwarderProps = new ProbeForwarderProps(probe)
}