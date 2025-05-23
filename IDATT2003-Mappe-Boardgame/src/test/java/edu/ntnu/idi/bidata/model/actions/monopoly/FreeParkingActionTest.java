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

class FreeParkingActionTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private Player mockPlayer;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
    mockPlayer = Mockito.mock(Player.class);
  }

  @AfterEach
  void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  @DisplayName("Constructor should initialize description")
  void testConstructor() {
    FreeParkingAction action = new FreeParkingAction("Free Parking Zone");

    action.perform(mockPlayer);
    assertTrue(outContent.toString().contains("Free Parking Zone"));
  }

  @Test
  @DisplayName("perform should print description")
  void testPerform() {
    FreeParkingAction action = new FreeParkingAction("Relax at Free Parking!");
    action.perform(mockPlayer);
    String consoleOutput = outContent.toString();
    assertTrue(consoleOutput.contains("Relax at Free Parking!"),
        "Console output mismatch. Actual: " + consoleOutput);
  }
}