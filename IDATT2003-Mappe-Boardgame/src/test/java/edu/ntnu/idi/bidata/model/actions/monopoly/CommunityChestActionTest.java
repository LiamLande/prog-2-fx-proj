// src/test/java/edu/ntnu/idi/bidata/model/actions/monopoly/CommunityChestActionTest.java
package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommunityChestActionTest {

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
    CommunityChestAction action = new CommunityChestAction("Community Chest Card");
    assertEquals("Community Chest Card", action.getDescription());
  }

  @Test
  @DisplayName("perform should print description and call service.drawCommunityChestCard")
  void testPerform_ServiceAvailable() {
    CommunityChestAction action = new CommunityChestAction("Draw a Community Chest card.");
    action.perform(mockPlayer);

    String consoleOutput = outContent.toString();
    assertTrue(consoleOutput.contains("Draw a Community Chest card."),
        "Console output mismatch. Actual: " + consoleOutput);
    verify(mockMonopolyService).drawCommunityChestCard(mockPlayer);
  }

  @Test
  @DisplayName("perform should print description and handle null service gracefully")
  void testPerform_NullService() {
    ServiceLocator.setMonopolyService(null); // Simulate service not being set
    CommunityChestAction action = new CommunityChestAction("Community Chest - No Service");

    // Should not throw NPE even if service is null, as it's checked in SUT
    assertDoesNotThrow(() -> action.perform(mockPlayer));

    String consoleOutput = outContent.toString();
    assertTrue(consoleOutput.contains("Community Chest - No Service"),
        "Console output mismatch. Actual: " + consoleOutput);
    // mockMonopolyService was never set globally to null, so this verify would fail if it was called.
    // Since the service IS null from the locator, drawCommunityChestCard should not be called on our mock instance.
    verify(mockMonopolyService, never()).drawCommunityChestCard(mockPlayer);
  }

  @Test
  @DisplayName("getDescription should return the correct description")
  void testGetDescription() {
    CommunityChestAction action = new CommunityChestAction("Test Description");
    assertEquals("Test Description", action.getDescription());
  }
}