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
import static org.mockito.Mockito.verify;

class TaxActionTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  private Player mockPlayer;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent)); // Capture System.out
    mockPlayer = Mockito.mock(Player.class);
  }

  @AfterEach
  void restoreStreams() {
    System.setOut(originalOut); // Restore System.out
  }

  @Test
  @DisplayName("Constructor should initialize description and taxAmount")
  void testConstructor() {
    TaxAction action = new TaxAction("Income Tax", 200);
    assertEquals("Income Tax", action.getDescription());
    assertEquals(200, action.getTaxAmount());
  }

  @Test
  @DisplayName("perform should call player.decreaseMoney and print message")
  void testPerform() {
    TaxAction action = new TaxAction("Luxury Tax", 75);
    action.perform(mockPlayer);

    verify(mockPlayer).decreaseMoney(75);
    String consoleOutput = outContent.toString();
    assertTrue(consoleOutput.contains("Luxury Tax You paid 75 in taxes."),
        "Console output mismatch. Actual: " + consoleOutput);
  }

  @Test
  @DisplayName("Getters should return correct values")
  void testGetters() {
    TaxAction action = new TaxAction("Property Tax", 150);
    assertEquals("Property Tax", action.getDescription());
    assertEquals(150, action.getTaxAmount());
  }
}