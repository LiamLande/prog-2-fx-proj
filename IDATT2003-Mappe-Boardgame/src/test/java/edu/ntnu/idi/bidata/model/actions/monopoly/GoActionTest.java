// src/test/java/edu/ntnu/idi/bidata/model/actions/monopoly/GoActionTest.java
package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

class GoActionTest {

  private Player mockPlayer;

  @BeforeEach
  void setUp() {
    mockPlayer = Mockito.mock(Player.class);
  }

  @Test
  @DisplayName("Constructor should initialize description and reward")
  void testConstructor() {
    // No getters for description/reward, but ensure no crash and perform works as expected
    GoAction action = new GoAction("Pass Go, Collect $200", 200);
    action.perform(mockPlayer); // Call perform to see effect based on constructor args
    verify(mockPlayer).increaseMoney(200);
  }

  @Test
  @DisplayName("perform should call player.increaseMoney with the reward amount")
  void testPerform_IncreasesMoney() {
    GoAction action = new GoAction("Landed on Go", 200);
    action.perform(mockPlayer);
    verify(mockPlayer).increaseMoney(200);
  }

  @Test
  @DisplayName("perform should handle zero reward")
  void testPerform_ZeroReward() {
    GoAction action = new GoAction("Go - No Reward This Time", 0);
    action.perform(mockPlayer);
    verify(mockPlayer).increaseMoney(0);
  }
}