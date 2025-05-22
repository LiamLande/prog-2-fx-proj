// src/test/java/edu/ntnu/idi/bidata/model/actions/monopoly/RailroadActionTest.java
package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RailroadActionTest {

  private Player mockPlayer;
  private Player mockOwner;
  private MonopolyService mockMonopolyService;

  @BeforeEach
  void setUp() {
    mockPlayer = mock(Player.class);
    mockOwner = mock(Player.class); // mockOwner and mockPlayer are distinct mock instances
    mockMonopolyService = mock(MonopolyService.class);

    ServiceLocator.setMonopolyService(mockMonopolyService);
  }

  @Test
  @DisplayName("Constructor should initialize with base rent")
  void testConstructor() {
    RailroadAction action = new RailroadAction("Reading Railroad", 200, 25);
    assertDoesNotThrow(() -> new RailroadAction("B&O Railroad", 200, 0));
    assertEquals(25, action.getRent(), "Rent should be base rent for railroads.");
  }

  @Test
  @DisplayName("perform should do nothing if owner is null")
  void testPerform_NoOwner() {
    RailroadAction action = new RailroadAction("Unowned RR", 200, 25);
    action.setOwner(null);
    action.perform(mockPlayer);
    verify(mockMonopolyService, never()).getRailroadsOwnedCount(any());
  }

  @Test
  @DisplayName("perform should do nothing if player is the owner")
  void testPerform_PlayerIsOwner() {
    RailroadAction action = new RailroadAction("Player's RR", 200, 25);
    action.setOwner(mockPlayer);
    action.perform(mockPlayer);
    verify(mockMonopolyService, never()).getRailroadsOwnedCount(any());
  }

  @Test
  @DisplayName("perform should calculate rent if owned by another (payment logic commented out)")
  void testPerform_OwnedByAnother() {
    RailroadAction action = new RailroadAction("Opponent's RR", 200, 25);
    action.setOwner(mockOwner);

    when(mockMonopolyService.getRailroadsOwnedCount(mockOwner)).thenReturn(2);

    action.perform(mockPlayer);

    verify(mockMonopolyService).getRailroadsOwnedCount(mockOwner);
  }

}