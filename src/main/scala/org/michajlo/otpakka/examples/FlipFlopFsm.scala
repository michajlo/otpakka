package org.michajlo.otpakka.examples
import org.michajlo.otpakka.behaviours.GenFsm
import org.michajlo.otpakka.behaviours.GenFsm.FsmState

class FlipFlopFsm extends GenFsm {

  def init(args: List[Any]) = ('ok, flip, 0)
  
  def flip: FsmState = {
    case ('change, stateData) =>
      println("in flip")
      ('next_state, flop, stateData)
  }
  
  def flop: FsmState = {
    case ('change, stateData) =>
      println("in flop")
      ('next_state, flip, stateData)
  }
  
  def terminate = {
    case (reason, state, stateData) => ()
  }
}