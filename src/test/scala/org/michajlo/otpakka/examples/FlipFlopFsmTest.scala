package org.michajlo.otpakka.examples
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class FlipFlopFsmTest {

  var underTest: FlipFlopFsm = _
  
  @Before
  def setUp() = {
    underTest = new FlipFlopFsm()
  }
  
  @Test
  def testInit() = {
    assertEquals(('ok, underTest.flipState, 0), underTest.init(Nil))
  }
  
  @Test
  def testFlipGoesToFlopOnChangeEvent() = {
    assertEquals(('next_state, underTest.flopState, 0), underTest.flip(('change, 0)))
  }
  
  @Test
  def testFlopGoesToFlipOnChangeEvent() = {
    assertEquals(('next_state, underTest.flipState, 0), underTest.flop(('change, 0)))
  }
}