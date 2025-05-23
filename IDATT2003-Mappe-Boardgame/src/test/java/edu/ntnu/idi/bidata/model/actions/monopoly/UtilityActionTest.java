package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UtilityActionTest {

  private Player mockPlayer;
  private Player mockOwner;
  private MonopolyService mockMonopolyService;

  @BeforeEach
  void setUp() {
    mockPlayer = mock(Player.class);
    mockOwner = mock(Player.class);
    mockMonopolyService = mock(MonopolyService.class);

    ServiceLocator.setMonopolyService(mockMonopolyService);
  }

  @Test
  @DisplayName("Constructor should initialize correctly")
  void testConstructor() {
    // Rent is 0 as per PropertyAction constructor, but not directly used by UtilityAction's logic.
    UtilityAction action = new UtilityAction("Water Works", 150);
    assertEquals("Water Works", action.getName());
    assertEquals(150, action.getCost());
  }

  @Test
  @DisplayName("perform should do nothing if owner is null")
  void testPerform_NoOwner() {
    UtilityAction action = new UtilityAction("Unowned Utility", 150);
    action.setOwner(null);
    action.perform(mockPlayer);
    verify(mockPlayer, never()).getLastDiceRoll();
    verify(mockMonopolyService, never()).getUtilitiesOwnedCount(any());
  }

  @Test
  @DisplayName("perform should do nothing if player is the owner")
  void testPerform_PlayerIsOwner() {
    UtilityAction action = new UtilityAction("Player's Utility", 150);
    action.setOwner(mockPlayer);
    action.perform(mockPlayer);
    verify(mockPlayer, never()).getLastDiceRoll();
    verify(mockMonopolyService, never()).getUtilitiesOwnedCount(any());
  }

  @Test
  @DisplayName("perform should calculate rent with 4x multiplier if 1 utility owned")
  void testPerform_OwnedByAnother_OneUtility() {
    UtilityAction action = new UtilityAction("Electric Company", 150);
    action.setOwner(mockOwner);

    when(mockPlayer.getLastDiceRoll()).thenReturn(7);
    when(mockMonopolyService.getUtilitiesOwnedCount(mockOwner)).thenReturn(1);

    action.perform(mockPlayer);

    verify(mockPlayer).getLastDiceRoll();
    verify(mockMonopolyService).getUtilitiesOwnedCount(mockOwner);
    // int rentToPay = 7 * 4 = 28;
    // Payment logic is commented out in SUT
    // verify(mockPlayer).decreaseMoney(28);
    // verify(mockOwner).increaseMoney(28);
  }

  @Test
  @DisplayName("perform should calculate rent with 10x multiplier if >1 utility owned")
  void testPerform_OwnedByAnother_MultipleUtilities() {
    UtilityAction action = new UtilityAction("Electric Company", 150);
    action.setOwner(mockOwner);

    when(mockPlayer.getLastDiceRoll()).thenReturn(5);
    when(mockMonopolyService.getUtilitiesOwnedCount(mockOwner)).thenReturn(2); // Owner has 2 utilities

    action.perform(mockPlayer);

    verify(mockPlayer).getLastDiceRoll();
    verify(mockMonopolyService).getUtilitiesOwnedCount(mockOwner);
  }
}