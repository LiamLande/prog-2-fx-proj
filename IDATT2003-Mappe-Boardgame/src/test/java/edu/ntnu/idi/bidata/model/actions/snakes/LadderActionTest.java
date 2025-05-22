// src/test/java/edu/ntnu/idi/bidata/model/actions/snakes/LadderActionTest.java
package edu.ntnu.idi.bidata.model.actions.snakes;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class LadderActionTest {

  @Test
  @DisplayName("Constructor should create LadderAction with valid parameters")
  void testConstructor_Valid() {
    LadderAction action = new LadderAction("Climb up!", 5);
    assertEquals(5, action.getSteps());
    // Description is not directly gettable, but constructor doesn't throw
    assertDoesNotThrow(() -> new LadderAction("Valid Ladder", 1));
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for null description")
  void testConstructor_NullDescription() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new LadderAction(null, 5));
    assertEquals("LadderAction description must not be empty", e.getMessage());
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for blank description")
  void testConstructor_BlankDescription() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new LadderAction("   ", 5));
    assertEquals("LadderAction description must not be empty", e.getMessage());
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for zero steps")
  void testConstructor_ZeroSteps() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new LadderAction("Test Ladder", 0));
    assertEquals("LadderAction steps must be positive", e.getMessage());
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for negative steps")
  void testConstructor_NegativeSteps() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new LadderAction("Test Ladder", -3));
    assertEquals("LadderAction steps must be positive", e.getMessage());
  }

  @Test
  @DisplayName("perform should call player.move with positive steps")
  void testPerform_CallsPlayerMove() {
    Player mockPlayer = Mockito.mock(Player.class);
    LadderAction action = new LadderAction("Go Up", 7);

    action.perform(mockPlayer);

    verify(mockPlayer).move(7);
  }

  @Test
  @DisplayName("getSteps should return the correct number of steps")
  void testGetSteps() {
    LadderAction action = new LadderAction("Test Ladder", 3);
    assertEquals(3, action.getSteps());
  }
}