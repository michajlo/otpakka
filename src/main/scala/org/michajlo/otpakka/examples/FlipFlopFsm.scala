package org.michajlo.otpakka.examples
import org.michajlo.otpakka.behaviours.GenFsm
import org.michajlo.otpakka.behaviours.GenFsm.FsmState

class FlipFlopFsm extends GenFsm {

  def init(args: List[Any]) = ('ok, flip, 0)
  
  def flip: FsmState = {
    case ('change, state) =>
      println("in flip")
      ('next_state, flop, state)
  }
  
  def flop: FsmState = {
    case ('change, state) =>
      println("in flop")
      ('next_state, flip, state)
  }
}