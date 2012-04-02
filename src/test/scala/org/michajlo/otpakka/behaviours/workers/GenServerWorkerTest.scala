package org.michajlo.otpakka.behaviours.workers
import org.junit.Assert._
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.michajlo.otpakka.behaviours.GenServer
import org.mockito.Mockito._
import org.mockito.Matchers
import org.mockito.Mockito

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.ImplicitSender
import akka.testkit.TestActorRef
import akka.util.duration._
import akka.dispatch.Await

class GenServerWorkerTest {
  
  implicit var actorSystem: ActorSystem = _
  
  var state: List[Any] = _
  
  var mockGenServer: GenServer = _
  
  var testActor: TestActorRef[GenServerWorker] = _
  
  var underTest: GenServerWorker = _
  
  @Before
  def setUp() = {
    actorSystem = ActorSystem("test")
    
    state = Nil
    mockGenServer = mock(classOf[GenServer])
    doReturn(('ok, state)).when(mockGenServer).init(Matchers.any(classOf[List[Any]]))
    
    testActor = TestActorRef(new GenServerWorker(mockGenServer, Nil))
    underTest = testActor.underlyingActor
  }

  @After
  def tearDown() = {
    actorSystem.shutdown()
  }
  
  // TODO: figure out how to test startup, akka has some weird rules about
  //       starting actors...
  
  @Test
  def testInitialStateIsAsReturnedByInit() = {
    // note, this was done in @Before
    assertEquals(Nil, underTest.state)
  }
  
  @Test
  def testGenCallMessagePassedOnToGenServerProperly() = {
    val msg = "Hello there"
    
    doReturn(('reply, 'ok, Nil)).when(mockGenServer).do_handle_call(Matchers.any(), Matchers.any(), Matchers.any())
    testActor ! ('gen_call, msg)
    verify(mockGenServer).do_handle_call(Matchers.eq(msg), Matchers.any(classOf[ActorRef]), Matchers.eq(Nil))
    ()
  }
  
  @Test
  def testHandleCallWithReplyAndNewStateProperlyUpdatesState() = {
    val newState = "State"
    
    doReturn(('reply, 'ok, newState)).when(mockGenServer).do_handle_call(Matchers.any(), Matchers.any(), Matchers.any())
    testActor ! ('gen_call, "blah")
    assertEquals(newState, underTest.state)
    ()
  }
  
  @Test
  def testHandleCallWithReplyRepliesToSender() = {
    val reply = "reply"
    
    doReturn(('reply, reply, Nil)).when(mockGenServer).do_handle_call(Matchers.any(), Matchers.any(), Matchers.any())
    val callFuture = (testActor ? ('gen_call, "blah"))(1 second)
    val result = Await.result(callFuture, 1 second)
    assertEquals(reply, result)
    ()
  }
  
  @Test
  def testHandleCallWithStopShutsDown() = {
    val reason = "why not"
    val state = Nil
    
    doReturn(('stop, reason, state)).when(mockGenServer).do_handle_call(Matchers.any(), Matchers.any(), Matchers.any())
    testActor ! ('gen_call, "blah")
    verify(mockGenServer).do_terminate(Matchers.eq(reason), Matchers.eq(state))
    assertTrue(testActor.isTerminated)
  }
  
  @Test
  def testGenCastMessagePassedOnToGenServerProperly() = {
    val msg = "Hello there"
    
    doReturn(('noreply, Nil)).when(mockGenServer).do_handle_cast(Matchers.any(), Matchers.any())
    testActor ! ('gen_cast, msg)
    verify(mockGenServer).do_handle_cast(Matchers.eq(msg), Matchers.eq(Nil))
    ()
  }
  
  @Test
  def testHandleCastWithNoReplyAndNewStateProperlyUpdatesState() = {
    val newState = "State"
    
    doReturn(('noreply, newState)).when(mockGenServer).do_handle_cast(Matchers.any(), Matchers.any())
    testActor ! ('gen_cast, "blah")
    assertEquals(newState, underTest.state)
    ()
  }
  
  def testHandleCastWithStopShutsDown() = {
    val reason = "why not"
    val state = Nil
    
    doReturn(('stop, reason, state)).when(mockGenServer).do_handle_cast(Matchers.any(), Matchers.any())
    testActor ! ('gen_cast, "blah")
    verify(mockGenServer).do_terminate(Matchers.eq(reason), Matchers.eq(state))
    assertTrue(testActor.isTerminated)
  }
  
  @Test
  def testUnclassifiedMessagesArePassedToHandleInfo() = {
    val msg = "Hello there"
    
    doReturn(('noreply, Nil)).when(mockGenServer).do_handle_info(Matchers.any(), Matchers.any())
    testActor ! msg
    verify(mockGenServer).do_handle_info(Matchers.eq(msg), Matchers.eq(Nil))
    ()
  } 
  
  @Test
  def testHandleInfoWithNoReplyAndNewStateProperlyUpdatesState() = {
    val newState = "State"
    
    doReturn(('noreply, newState)).when(mockGenServer).do_handle_info(Matchers.any(), Matchers.any())
    testActor ! "blah"
    assertEquals(newState, underTest.state)
    ()
  }
  
  def testHandleInfoWithStopShutsDown() = {
    val reason = "why not"
    val state = Nil
    
    doReturn(('stop, reason, state)).when(mockGenServer).do_handle_info(Matchers.any(), Matchers.any())
    testActor ! "blah"
    verify(mockGenServer).do_terminate(Matchers.eq(reason), Matchers.eq(state))
    assertTrue(testActor.isTerminated)
  }
}