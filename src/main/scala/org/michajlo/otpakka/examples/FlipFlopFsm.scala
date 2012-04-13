package org.michajlo.otpakka.examples
import org.michajlo.otpakka.behaviours.GenFsm

class FlipFlopFsm extends GenFsm {

  def init(args: List[Any]) = ('ok, flip, 0)
  
  def flip: PartialFunction[(Any, Any), Any] = {
    case ('change, state) =>
      println("in flip")
      ('next_state, flop, state)
  }
  
  def flop: PartialFunction[(Any, Any), Any] = {
    case ('change, state) =>
      println("in flop")
      ('next_state, flip, state)
  }
}