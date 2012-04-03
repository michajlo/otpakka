package org.michajlo.otpakka.behaviours

import org.michajlo.otpakka.behaviours.workers.GenServerWorker

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await

/**
 * Object for simplifying client interactions with and creation of a GenServer.
 */
object GenServer {
  
  implicit val as: ActorSystem = ActorSystem("genserver")
  
  /**
   * Create a new {@link GenServer} with the default name (name of the class)
   * 
   * @param cls {@link Class} implementin {@link GenServer} to create
   * @param args a List[Any] of arguments, passed to {@link GenServer#init}
   * 
   * @return an {@link ActorRef} for the created instance
   */
  def start[T <: GenServer](cls: Class[T], args: List[Any]): ActorRef =
    start(cls.getName(), cls, args)
  
  /**
   * Create a new {@link GenServer}
   * 
   * @param name name for {@link ActorRef}
   * @param cls {@link Class} implementin {@link GenServer} to create
   * @param args a List[Any] of arguments, passed to {@link GenServer#init}
   * 
   * @return an {@link ActorRef} for the created instance
   */
  def start[T <: GenServer](name: String, cls: Class[T], args: List[Any]): ActorRef = {
    val instance =  cls.newInstance()
    as.actorOf(Props(new GenServerWorker(instance, args)), name=name)
  }
    
  /**
   * Call the {@link GenServer} identified by ref, with the default timeout (5 seconds)
   * 
   * @param ref {@link ActorRef} identifying the GenServer to interact with
   * @param msg message to send the instance
   * 
   * @return value returned by the server, or ('error, reason: Exception)
   */
  def call(ref: ActorRef, msg: Any): Any =  call(ref, msg, 5 seconds)
  
  /**
   * Call the {@link GenServer} identified by ref
   * 
   * @param ref {@link ActorRef} identifying the GenServer to interact with
   * @param msg message to send the instance
   * @param timeout how long to wait for a response
   * 
   * @return value returned by the server, or ('error, reason: Exception)
   */
  def call(ref: ActorRef, msg: Any, timeout: Timeout): Any = {
    try {
      val future = (ref ? ('gen_call, msg))(timeout)
      Await.result(future, timeout.duration)
    } catch {
      case e: Exception => ('error, e)
    }
  }
  
  /**
   * Cast (asynchonously send a message) to the {@link GenServer} identified by ref
   * 
   * @param ref {@link ActorRef} identifying the GenServer to interact with
   * @param msg message to send the instance
   */
  def cast(ref: ActorRef, msg: Any) = ref ! (('gen_cast, msg))
}

/**
 * A trait defining behaviours for a generic server process which expects "calls" and "casts", which
 * are messages which are typically responded to, or handled asynchronously, respectively.
 * 
 * GenServer instances should be created via {@link GenServer#start} and interacted with via the commands
 * provided by the {@link GenServer} object
 */
trait GenServer {

  /**
   * Initializer, called by {@link GenServer#start} to create this instance.
   * 
   * @param args arguments in list form
   
   * @return ('ok, state: Any) on success, the instances state will be initialized to state
   *         ('error, reason: Any) on failure
   */
  def init(args: List[Any]): (Any, Any)
  
  /**
   * Handle a {@link GenServer#call}.  Calls typically expect responses.
   * 
   * The {@link PartialFunction} should take a tuple of (Any, ActorRef, Any), where the elements
   * are (message, from, state) and return one of:
   *    ('reply, reply: Any, newState: Any) to reply to the sender and update the state
   *        with newState
   *    ('stop, reason: Any, newState: Any) to stop this instance. When stopping terminate
   *        is called with (reason, newState)
   */
  def handle_call: PartialFunction[(Any, ActorRef, Any), Any]
  
  /**
   * Handle a {@link GenServer#cast}.  Casts do not expect responses.
   * 
   * The {@link PartialFunction} should take a tuple of (Any, Any), where the elements
   * are (message, state) and return one of:
   *    ('noreply, newState: Any) to update the state with newState
   *    ('stop, reason: Any, newState: Any) to stop this instance. When stopping terminate
   *        is called with (reason, newState)
   */
  def handle_cast: PartialFunction[(Any, Any), Any]
  
  /**
   * Handle a miscellaneous message.  This is a catch all for messages
   * not intended for call or cast.
   * 
   * The {@link PartialFunction} should take a tuple of (Any, Any) where the elements
   * are (message, state) and return one of:
   *    ('noreply, newState: Any) to update the state with newState
   *    ('stop, reason: Any, newState: Any) to stop this instance. When stopping terminate
   *        is called with (reason, newState)
   */
  def handle_info: PartialFunction[(Any, Any), Any]
  
  /**
   * Called on termination to allow cleanup.
   * 
   * The {@link PartialFunction} should take a tuple of (Any, Any) where the elements
   * are (reason, state) and return Unit
   */
  def terminate: PartialFunction[(Any, Any), Unit]
  
  def do_handle_call(msg: Any, from: ActorRef, state: Any): Any =
    handle_call((msg, from, state))
    
  def do_handle_cast(msg: Any, state: Any): Any =
    handle_cast((msg, state))
    
  def do_handle_info(msg: Any, state: Any): Any =
    handle_info((msg, state))
    
  def do_terminate(reason: Any, state: Any): Unit =
    terminate((reason, state))
}