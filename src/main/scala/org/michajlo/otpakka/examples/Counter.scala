package org.michajlo.otpakka.examples
import org.michajlo.otpakka.behaviours.GenServer

import akka.actor.ActorRef

object Counter {
  
  var ref: ActorRef = _
  
  def start() = {
    ref = GenServer.start(classOf[Counter], Nil)
  }
  
  def getCount() = GenServer.call(ref, 'getCount)
  
  def increment() = GenServer.cast(ref, 'increment)
  
}

class Counter extends GenServer {
    
  def init(args: List[Any]) = ('ok, 0)
    
  def handle_call = {
    case ('getCount, from, count: Int) =>
      ('reply, count, count)
  }
  
  def handle_cast = {
    case ('increment, count: Int) =>
      ('noreply, count+1)
  }
}