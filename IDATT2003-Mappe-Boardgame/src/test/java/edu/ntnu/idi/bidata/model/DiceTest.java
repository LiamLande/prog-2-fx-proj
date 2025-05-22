// src/test/java/edu/ntnu/idi/bidata/model/DiceTest.java
package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiceTest {

  @Test
  @DisplayName("Constructor should create dice for valid number")
  void testConstructor_ValidNumberOfDice() {
    assertDoesNotThrow(() -> new Dice(1));
    assertDoesNotThrow(() -> new Dice(2));
    assertDoesNotThrow(() -> new Dice(5));
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for zero dice")
  void testConstructor_ZeroDice() {
    Exception exception = assertThrows(InvalidParameterException.class, () -> new Dice(0));
    assertEquals("Must create at least one die", exception.getMessage());
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for negative dice")
  void testConstructor_NegativeDice() {
    Exception exception = assertThrows(InvalidParameterException.class, () -> new Dice(-1));
    assertEquals("Must create at least one die", exception.getMessage());
  }

  @RepeatedTest(10)
  @DisplayName("rollDie should return sum within expected range for one die")
  void testRollDie_OneDie() {
    Dice dice = new Dice(1);
    int sum = dice.rollDie();
    assertTrue(sum >= 1 && sum <= 6, "Sum for one die should be between 1 and 6. Was: " + sum);
  }

  @RepeatedTest(20)
  @DisplayName("rollDie should return sum within expected range for two dice")
  void testRollDie_TwoDice() {
    Dice dice = new Dice(2);
    int sum = dice.rollDie();
    assertTrue(sum >= 2 && sum <= 12, "Sum for two dice should be between 2 and 12. Was: " + sum);
  }

  @Test
  @DisplayName("getDie should return value of specific die after roll")
  void testGetDie_ValidIndex() {
    Dice dice = new Dice(3);
    dice.rollDie(); // Roll all dice to ensure they have values

    // We can't predict the exact value, but it should be between 1 and 6
    int die0Value = dice.getDie(0);
    assertTrue(die0Value >= 1 && die0Value <= 6, "Die 0 value out of range.");

    int die1Value = dice.getDie(1);
    assertTrue(die1Value >= 1 && die1Value <= 6, "Die 1 value out of range.");

    int die2Value = dice.getDie(2);
    assertTrue(die2Value >= 1 && die2Value <= 6, "Die 2 value out of range.");
  }

  @Test
  @DisplayName("getDie should return 0 for a die that has not been individually rolled as part of rollDie()")
  void testGetDie_BeforeIndividualRoll() {
    Dice dice = new Dice(1);
    // Die.lastRolledValue is 0 by default
    assertEquals(0, dice.getDie(0), "Value of die before rollDie() or individual roll should be 0");
  }


  @Test
  @DisplayName("getDie should throw InvalidParameterException for negative index")
  void testGetDie_NegativeIndex() {
    Dice dice = new Dice(2);
    Exception exception = assertThrows(InvalidParameterException.class, () -> dice.getDie(-1));
    assertEquals("Die index out of range: -1", exception.getMessage());
  }

  @Test
  @DisplayName("getDie should throw InvalidParameterException for index out of bounds")
  void testGetDie_IndexOutOfBounds() {
    Dice dice = new Dice(2);
    Exception exception = assertThrows(InvalidParameterException.class, () -> dice.getDie(2));
    assertEquals("Die index out of range: 2", exception.getMessage());
  }
}