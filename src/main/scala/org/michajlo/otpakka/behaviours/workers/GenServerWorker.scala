package org.michajlo.otpakka.behaviours.workers

import org.michajlo.otpakka.behaviours.GenServer
import akka.actor.Actor

class GenServerWorker(val genServer: GenServer, args: List[Any]) extends Actor {

  var state: Any = _
  
  genServer.init(args) match {
    case ('ok, initState: Any) => state = initState
    case ('error, reason: Any) => throw new RuntimeException(reason.toString())
  }
  
  def receive = {
    case ('gen_call, msg) =>
      genServer.handle_call((msg, sender, state)) match {
        case ('reply, reply, newState) =>
          state = newState
          sender ! reply
      }
      
    case ('gen_cast, msg) =>
      genServer.handle_cast((msg, state)) match {
        case ('noreply, newState) =>
          state = newState
      }
            
  }
  
}