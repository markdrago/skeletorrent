package tracker

import akka.actor.ActorRef

trait HttpClientComponent {
  val httpClient: ActorRef
}
