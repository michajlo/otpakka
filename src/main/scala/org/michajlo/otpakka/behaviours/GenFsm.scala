package org.michajlo.otpakka.behaviours
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import org.michajlo.otpakka.behaviours.workers.GenFsmWorker

object GenFsm {
  implicit val as: ActorSystem = ActorSystem("genfsm")

  def start[T <: GenFsm](cls: Class[T], args: List[Any]): ActorRef =
    start(cls.getName(), cls, args)
    
  def start[T <: GenFsm](name: String, cls: Class[T], args: List[Any]): ActorRef = {
    val instance = cls.newInstance()
    as.actorOf(Props(new GenFsmWorker(instance, args)), name=name)
  }
  
  def send_event(ref: ActorRef, event: Any) = ref ! ('gen_event, event)
}

trait GenFsm {
    def init(args: List[Any]): Any
}