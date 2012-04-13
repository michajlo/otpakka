package org.michajlo.otpakka.examples
import org.michajlo.otpakka.behaviours.GenFsm
import org.michajlo.otpakka.behaviours.GenFsm.FsmState

class FlipFlopFsm extends GenFsm {

  // This isn't necessary, but it makes comparing functions
  // easier and in turn testing much easier as well
  val flipState = flip
  val flopState = flop
  
  def init(args: List[Any]) = ('ok, flipState, 0)
  
  def flip: FsmState = {
    case ('change, stateData) =>
      ('next_state, flopState, stateData)
  }
  
  def flop: FsmState = {
    case ('change, stateData) =>
      ('next_state, flipState, stateData)
  }
  
  def terminate = {
    case (reason, state, stateData) => ()
  }
}