package org.michajlo.otpakka.behaviours.workers
import org.michajlo.otpakka.behaviours.GenFsm
import akka.actor.Actor

class GenFsmWorker(val genFsm: GenFsm, args: List[Any]) extends Actor {
  var state: PartialFunction[(Any, Any), Any] = _
  var stateData: Any = _
    
  genFsm.init(args) match {
    case ('ok, initState: PartialFunction[(Any, Any), Any], initData) =>
      state = initState
      stateData = initData
    case ('error, reason: Any) => throw new RuntimeException(reason.toString())
  }
  
  def receive = {
    case ('gen_event, event) => 
      state((event, stateData)) match {
        case ('next_state, newState: PartialFunction[(Any, Any), Any], newStateData) =>
          state = newState
          stateData = newStateData
      }
  }
}