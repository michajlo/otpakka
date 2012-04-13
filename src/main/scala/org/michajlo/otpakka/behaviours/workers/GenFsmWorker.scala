package org.michajlo.otpakka.behaviours.workers
import org.michajlo.otpakka.behaviours.GenFsm
import org.michajlo.otpakka.behaviours.GenFsm.FsmState
import akka.actor.Actor

class GenFsmWorker(val genFsm: GenFsm, args: List[Any]) extends Actor {
  var state: FsmState = _
  var stateData: Any = _
    
  genFsm.init(args) match {
    case ('ok, initState: FsmState, initData) =>
      state = initState
      stateData = initData
    case ('error, reason: Any) => throw new RuntimeException(reason.toString())
  }
  
  def receive = {
    case ('gen_event, event) => 
      state((event, stateData)) match {
        case ('next_state, newState: FsmState, newStateData) =>
          state = newState
          stateData = newStateData
        case ('stop, reason, newStateData) =>
          stateData = newStateData
          genFsm.do_terminate(reason, state, newStateData)
          context.stop(self)
      }
  }
}