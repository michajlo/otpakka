package org.michajlo.otpakka.behaviours
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import org.michajlo.otpakka.behaviours.workers.GenFsmWorker

/**
 * Object for simplifying client interactions with and creation of a GenFsm
 */
object GenFsm {
  
  type FsmState = PartialFunction[(Any, Any), Any]

  implicit val as: ActorSystem = ActorSystem("genfsm")

    
  /**
   * Create a new {@link GenFsm} with the default name (name of the class)
   * 
   * @param cls {@link Class} implementing GenServer to create
   * @param args a List[Any] of arguments, passed to {@link GenFsm#init}
   * 
   * @return an {@link ActorRef} for the created instance
   */
  def start[T <: GenFsm](cls: Class[T], args: List[Any]): ActorRef =
    start(cls.getName(), cls, args)
    
    /**
   * Create a new {@link GenFsm}
   * 
   * @param name name for {@link ActorRef}
   * @param cls {@link Class} implementing {@link GenFsm} to create
   * @param args a List[Any] of arguments, passed to {@link GenFsm#init}
   * 
   * @return an {@link ActorRef} for the created instance
   */
  def start[T <: GenFsm](name: String, cls: Class[T], args: List[Any]): ActorRef = {
    val instance = cls.newInstance()
    as.actorOf(Props(new GenFsmWorker(instance, args)), name=name)
  }
  
  /**
   * Send an event to the {@link GenFsm} identified by ref
   * 
   * @param ref {@link ActorRef} identifying the GenFsm to interact with
   * @param event event to send the instance
   */
  def send_event(ref: ActorRef, event: Any) = ref ! ('gen_event, event)
}

/**
 * A trait defining behaviours for a generic finite state machine process.
 * 
 * GenFsm instances should be created via {@link GenFsm#start} and interacted with via the commands
 * provided by the {@link GenFsm} object
 */
trait GenFsm {
  
  /**
   * Initializer, called by {@link GenFsm#start} to create this instance.
   * 
   * @param args arguments in list form
   
   * @return ('ok, state: FsmState, stateData: Any) on success, the instances state will be initialized to state,
   *            with stateData as the state's data.  stateData is equivalent to state in {@link GenServer}
   *         ('error, reason: Any) on failure
   */
    def init(args: List[Any]): Any
}