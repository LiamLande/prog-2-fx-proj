package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChanceActionTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  private Player mockPlayer;
  private MonopolyService mockMonopolyService;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
    mockPlayer = mock(Player.class);
    mockMonopolyService = mock(MonopolyService.class);
    ServiceLocator.setMonopolyService(mockMonopolyService);
  }

  @AfterEach
  void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  @DisplayName("Constructor should initialize description")
  void testConstructor() {
    ChanceAction action = new ChanceAction("Chance Card");
    assertEquals("Chance Card", action.getDescription());
  }

  @Test
  @DisplayName("perform should print description and call service.drawChanceCard")
  void testPerform_ServiceAvailable() {
    ChanceAction action = new ChanceAction("Draw a Chance card.");
    action.perform(mockPlayer);

    String consoleOutput = outContent.toString();
    assertTrue(consoleOutput.contains("Draw a Chance card."),
        "Console output mismatch. Actual: " + consoleOutput);
    verify(mockMonopolyService).drawChanceCard(mockPlayer);
  }

  @Test
  @DisplayName("perform should print description and handle null service gracefully")
  void testPerform_NullService() {
    ServiceLocator.setMonopolyService(null); // Simulate service not being set
    ChanceAction action = new ChanceAction("Chance - No Service");

    assertDoesNotThrow(() -> action.perform(mockPlayer)); // SUT checks for null service

    String consoleOutput = outContent.toString();
    assertTrue(consoleOutput.contains("Chance - No Service"),
        "Console output mismatch. Actual: " + consoleOutput);
    verify(mockMonopolyService, never()).drawChanceCard(mockPlayer); // Should not be called on our mock
  }

  @Test
  @DisplayName("getDescription should return the correct description")
  void testGetDescription() {
    ChanceAction action = new ChanceAction("Test Chance Description");
    assertEquals("Test Chance Description", action.getDescription());
  }
}