package edu.ntnu.idi.bidata.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DieTest {

  private Die die;

  @BeforeEach
  void setUp() {
    die = new Die();
  }

  @Test
  @DisplayName("getValue before first roll should return 0")
  void testGetValue_beforeFirstRoll_returnsZero() {
    // lastRolledValue is an int, defaults to 0. Javadoc says "undefined".
    // Testing for 0 matches current implementation.
    assertEquals(0, die.getValue(), "Value before first roll should be 0.");
  }

  @RepeatedTest(20)
  @DisplayName("roll() should return a value between 1 and 6")
  void testRoll_returnsValueInValidRange() {
    int rolledValue = die.roll();
    assertTrue(rolledValue >= 1 && rolledValue <= 6,
        "Rolled value (" + rolledValue + ") should be between 1 and 6 (inclusive).");
  }

  @Test
  @DisplayName("getValue after roll() should return the last rolled value")
  void testGetValue_afterRoll_returnsLastRolledValue() {
    int firstRoll = die.roll();
    assertEquals(firstRoll, die.getValue(), "getValue() should return the value from the first roll.");

    int secondRoll = die.roll();
    assertEquals(secondRoll, die.getValue(), "getValue() should return the value from the second roll.");
    assertNotEquals(firstRoll, die.getValue(),
        "getValue() should update after a new roll, assuming rolls are different (statistically likely). This might rarely fail if rolls are identical.");
  }

  @Test
  @DisplayName("Multiple rolls should update lastRolledValue consistently")
  void testMultipleRolls_updatesValue() {
    // This test primarily ensures getValue updates correctly after each roll.
    // It doesn't assert randomness, just consistency.
    for (int i = 0; i < 10; i++) {
      int rolledValue = die.roll();
      assertEquals(rolledValue, die.getValue(),
          "getValue() should match the most recent roll().");
      assertTrue(rolledValue >= 1 && rolledValue <= 6,
          "Each rolled value should be within 1-6.");
    }
  }
}