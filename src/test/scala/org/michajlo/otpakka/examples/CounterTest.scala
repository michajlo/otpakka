package org.michajlo.otpakka.examples
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class CounterTest {

  var underTest: Counter = _
  
  @Before
  def setUp() = {
    underTest = new Counter()
  }
  
  @Test
  def testInitInitsStateTo0() = {
    assertEquals(('ok, 0), underTest.init(Nil))
  }
  
  @Test
  def testCastIncrementUpdatesCount() = {
    assertEquals(('noreply, 42), underTest.handle_cast(('increment, 41)))
  }
  
  @Test
  def testCallRepliesWithCountAndDoesntUpdateState() = {
    assertEquals(('reply, 42, 42), underTest.handle_call(('getCount, null, 42)))
  }
  
  @Test
  def testHandleInfoDoesntModifyState() = {
    assertEquals(('noreply, 42), underTest.handle_info(("blah blah blah", 42)))
  }
}