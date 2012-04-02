package org.michajlo.otpakka.behaviours

import org.michajlo.otpakka.behaviours.workers.GenServerWorker

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await

object GenServer {
  
  implicit val as: ActorSystem = ActorSystem("genserver")
  
  def start[T <: GenServer](cls: Class[T], args: List[Any]): ActorRef =
    start(cls.getName(), cls, args)
  
  def start[T <: GenServer](name: String, cls: Class[T], args: List[Any]): ActorRef = {
    val instance =  cls.newInstance()
    as.actorOf(Props(new GenServerWorker(instance, args)), name=name)
  }
    
  def call(ref: ActorRef, msg: Any): Any =  call(ref, msg, 5 seconds)
  
  def call(ref: ActorRef, msg: Any, timeout: Timeout): Any = {
    try {
      val future = (ref ? ('gen_call, msg))(timeout)
      Await.result(future, timeout.duration)
    } catch {
      case e: Exception => ('error, e)
    }
  }
  
  def cast(ref: ActorRef, msg: Any) = ref ! (('gen_cast, msg))
}

trait GenServer {

  def init(args: List[Any]): (Any, Any)
  
  def handle_call: PartialFunction[(Any, ActorRef, Any), (Symbol, Any, Any)]
  
  def handle_cast: PartialFunction[(Any, Any), (Symbol, Any)]
  
  def handle_info: PartialFunction[(Any, Any), (Symbol, Any)]
  
  def do_handle_call(msg: Any, from: ActorRef, state: Any): (Symbol, Any, Any) =
    handle_call((msg, from, state))
    
  def do_handle_cast(msg: Any, state: Any): (Symbol, Any) =
    handle_cast((msg, state))
}