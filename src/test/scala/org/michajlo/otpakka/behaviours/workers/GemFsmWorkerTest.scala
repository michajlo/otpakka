package org.michajlo.otpakka.behaviours.workers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.michajlo.otpakka.behaviours.GenFsm
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Matchers

import akka.actor.ActorSystem
import akka.testkit.TestActorRef

class GemFsmWorkerTest {
  import GenFsm.FsmState

  implicit var actorSystem: ActorSystem = _
  
  var state: FsmState = _
  
  var stateData: List[Any] = _
  
  var mockGenFsm: GenFsm = _
  
  var testActor: TestActorRef[GenFsmWorker] = _
  
  var underTest: GenFsmWorker = _
  
  @Before
  def setUp() = {
    actorSystem = ActorSystem("test")
    
    state = mock(classOf[FsmState])
    stateData = Nil
    mockGenFsm = mock(classOf[GenFsm])
    doReturn(('ok, state, stateData)).when(mockGenFsm).init(Matchers.any(classOf[List[Any]]))
    
    testActor = TestActorRef(new GenFsmWorker(mockGenFsm, Nil))
    underTest = testActor.underlyingActor
  }
  
  @After
  def tearDown() = {
    actorSystem.shutdown()
  }
  
  @Test
  def testInitialStateIsAsReturnedByInit() = {
    // note, this was done in @Before
    assertEquals(state, underTest.state)
    assertEquals(Nil, underTest.stateData)
  }
  
  @Test
  def testStateTransitionsProperly() = {
    val nextState = mock(classOf[FsmState])
    val nextStateData = "a" :: Nil
    val event = 'some_event
    
    doReturn(('next_state, nextState, nextStateData)).when(state).apply(Matchers.any(classOf[(Any, Any)]))
    
    testActor ! ('gen_event, event)
    
    verify(state).apply(Matchers.eq((event, stateData)))
    assertEquals(nextState, underTest.state)
    assertEquals(nextStateData, underTest.stateData)
  }
  
  @Test
  def testStateTransitionWithStopShutsDown() = {
    val reason = "why not"
    val newStateData = "a" :: Nil
    val event = 'some_event
    
    doReturn(('stop, reason, newStateData)).when(state).apply(Matchers.any(classOf[(Any, Any)]))
    
    testActor ! ('gen_event, event)
    
    verify(mockGenFsm).do_terminate(Matchers.eq(reason), Matchers.eq(state), Matchers.eq(newStateData))
    assertTrue(testActor.isTerminated)
  }
}