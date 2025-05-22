// src/test/java/edu/ntnu/idi/bidata/model/actions/monopoly/JailActionTest.java
package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class JailActionTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private Player mockPlayer;


  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
    mockPlayer = Mockito.mock(Player.class); // Though not directly used by JailAction's perform
  }

  @AfterEach
  void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  @DisplayName("Constructor should initialize description")
  void testConstructor() {
    JailAction action = new JailAction("Just Visiting Jail");
    // Description is private without a getter, so we verify by perform's output
    action.perform(mockPlayer); // Player arg isn't used by SUT's perform
    assertTrue(outContent.toString().contains("Just Visiting Jail"), "Description mismatch in output.");
  }

  @Test
  @DisplayName("perform should print description")
  void testPerform() {
    JailAction action = new JailAction("You are in Jail (Just Visiting).");
    action.perform(mockPlayer); // Player arg isn't used by SUT's perform
    String consoleOutput = outContent.toString();
    assertTrue(consoleOutput.contains("You are in Jail (Just Visiting)."),
        "Console output mismatch. Actual: " + consoleOutput);
  }
}