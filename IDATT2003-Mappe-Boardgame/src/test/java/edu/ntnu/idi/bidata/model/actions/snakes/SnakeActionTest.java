package edu.ntnu.idi.bidata.model.actions.snakes;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class SnakeActionTest {

  @Test
  @DisplayName("Constructor should create SnakeAction with valid parameters")
  void testConstructor_Valid() {
    SnakeAction action = new SnakeAction("Slide down!", 5);
    assertEquals(5, action.getSteps());

    assertDoesNotThrow(() -> new SnakeAction("Valid Snake", 1));
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for null description")
  void testConstructor_NullDescription() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new SnakeAction(null, 5));
    assertEquals("SnakeAction description must not be empty", e.getMessage());
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for blank description")
  void testConstructor_BlankDescription() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new SnakeAction("   ", 5));
    assertEquals("SnakeAction description must not be empty", e.getMessage());
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for zero steps")
  void testConstructor_ZeroSteps() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new SnakeAction("Test Snake", 0));
    assertEquals("SnakeAction steps must be positive", e.getMessage());
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for negative steps")
  void testConstructor_NegativeSteps() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new SnakeAction("Test Snake", -3));
    assertEquals("SnakeAction steps must be positive", e.getMessage());
  }

  @Test
  @DisplayName("perform should call player.move with negative steps")
  void testPerform_CallsPlayerMove() {
    Player mockPlayer = Mockito.mock(Player.class);
    SnakeAction action = new SnakeAction("Go Down", 7);

    action.perform(mockPlayer);

    verify(mockPlayer).move(-7); // Note the negative sign
  }

  @Test
  @DisplayName("getSteps should return the correct number of steps")
  void testGetSteps() {
    SnakeAction action = new SnakeAction("Test Snake", 3);
    assertEquals(3, action.getSteps());
  }
}