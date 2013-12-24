package utils

import akka.actor.{ActorRef, ActorSystem, Actor, Props}

//useful when testing that an actor sends messages to its parent

object ParentChildMediator {
  //create a "parent" actor and a child defined by the provided childProps
  //the "parent" actor forwards all messages to the child unless
  //the message was sent by the child, in which case the parent sends
  //that message to the provided fosterParent ActorRef
  def apply(fosterParent: ActorRef, childProps: Props)(implicit system: ActorSystem): ActorRef = {
    system.actorOf(Props(classOf[ParentChildMediatorActor], fosterParent, childProps), fosterParent.path
      .name + "-PCMediator-parent")
  }
}

private class ParentChildMediatorActor(fosterParent: ActorRef, childProps: Props) extends Actor {
  val child = context.actorOf(childProps, fosterParent.path.name + "-PCMediator-child")

  def receive = {
    case x if sender == child => fosterParent forward x
    case x                    => child forward x
  }
}