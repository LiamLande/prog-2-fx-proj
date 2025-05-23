package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class PropertyActionTest {

  private Player mockPlayer;
  private Player mockOwner;

  @BeforeEach
  void setUp() {
    mockPlayer = Mockito.mock(Player.class);
    mockOwner = Mockito.mock(Player.class);
  }

  @Test
  @DisplayName("Constructor (name, cost, rent) should initialize correctly with null colorGroup")
  void testConstructor_ThreeArgs() {
    PropertyAction action = new PropertyAction("Boardwalk", 400, 50);
    assertEquals("Boardwalk", action.getName());
    assertEquals(400, action.getCost());
    assertEquals(50, action.getRent()); // This is the base rent field
    assertNull(action.getOwner());
    assertNull(action.getColorGroup());
  }

  @Test
  @DisplayName("Constructor (name, cost, rent, colorGroup) should initialize correctly")
  void testConstructor_FourArgs() {
    PropertyAction action = new PropertyAction("Park Place", 350, 35, "Blue");
    assertEquals("Park Place", action.getName());
    assertEquals(350, action.getCost());
    assertEquals(35, action.getRent());
    assertEquals("Blue", action.getColorGroup());
    assertNull(action.getOwner());
  }

  @Test
  @DisplayName("setOwner and getOwner should work correctly")
  void testSetAndGetOwner() {
    PropertyAction action = new PropertyAction("Test Property", 100, 10);
    assertNull(action.getOwner(), "Owner should be null initially.");
    action.setOwner(mockOwner);
    assertEquals(mockOwner, action.getOwner(), "getOwner should return the set owner.");
    action.setOwner(null); // Test setting owner back to null
    assertNull(action.getOwner(), "Owner should be nullable.");
  }

  @Test
  @DisplayName("perform should currently do nothing (logic commented out or handled by UI)")
  void testPerform_CurrentImplementation() {
    PropertyAction action = new PropertyAction("Some Property", 200, 20);
    // Test with no owner
    assertDoesNotThrow(() -> action.perform(mockPlayer), "Perform with no owner should not throw.");

    // Test with owner being the player
    action.setOwner(mockPlayer);
    assertDoesNotThrow(() -> action.perform(mockPlayer), "Perform with player as owner should not throw.");

    // Test with owner being another player
    action.setOwner(mockOwner);
    Player anotherPlayer = Mockito.mock(Player.class); // The player landing on the tile
    assertDoesNotThrow(() -> action.perform(anotherPlayer), "Perform with another player as owner should not throw.");
  }

  @Test
  @DisplayName("calculateRent should return base rent value by default")
  void testCalculateRent_Default() {
    PropertyAction action = new PropertyAction("Utility", 150, 15); // Base rent is 15
    assertEquals(15, action.calculateRent(), "Default calculateRent should return the base rent field.");
  }

  @Test
  @DisplayName("Getters for name, cost, rent, and colorGroup should return correct values")
  void testGetters() {
    PropertyAction actionWithColor = new PropertyAction("Mediterranean Avenue", 60, 2, "Brown");
    assertEquals("Mediterranean Avenue", actionWithColor.getName());
    assertEquals(60, actionWithColor.getCost());
    assertEquals(2, actionWithColor.getRent());
    assertEquals("Brown", actionWithColor.getColorGroup());

    PropertyAction actionWithoutColor = new PropertyAction("Reading Railroad", 200, 25);
    assertEquals("Reading Railroad", actionWithoutColor.getName());
    assertEquals(200, actionWithoutColor.getCost());
    assertEquals(25, actionWithoutColor.getRent());
    assertNull(actionWithoutColor.getColorGroup(), "Color group should be null when not provided.");
  }
}