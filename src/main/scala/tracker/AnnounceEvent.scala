package tracker

sealed trait AnnounceEvent { val name: String }
case class AnnounceEventStarted() extends AnnounceEvent { override val name = "started" }
case class AnnounceEventStopped() extends AnnounceEvent { override val name = "stopped" }
case class AnnounceEventCompleted() extends AnnounceEvent { override val name = "completed" }
