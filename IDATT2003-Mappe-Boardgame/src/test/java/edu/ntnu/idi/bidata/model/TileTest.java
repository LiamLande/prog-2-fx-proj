package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // For Mockito annotations like @Mock
class TileTest {

  @Nested
  @DisplayName("Constructor and ID Tests")
  class ConstructorAndIdTests {

    @Test
    @DisplayName("Should create Tile with valid non-negative ID")
    void testConstructor_withValidId_createsTile() {
      Tile tile1 = new Tile(0);
      assertEquals(0, tile1.getId(), "Tile ID should be 0");

      Tile tile2 = new Tile(10);
      assertEquals(10, tile2.getId(), "Tile ID should be 10");
    }

    @Test
    @DisplayName("Should throw InvalidParameterException for negative ID")
    void testConstructor_withNegativeId_throwsInvalidParameterException() {
      InvalidParameterException exception = assertThrows(
          InvalidParameterException.class,
          () -> new Tile(-1),
          "Constructor should throw InvalidParameterException for negative ID"
      );
      assertEquals("Tile id must be non‐negative", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("Getter and Setter Tests")
  class GetterSetterTests {
    private Tile tile;
    @Mock // Mockito will create a mock Tile instance
    private Tile mockNextTile;
    @Mock
    private Tile mockPreviousTile;
    @Mock // Mockito will create a mock TileAction instance
    private TileAction mockTileAction;


    @BeforeEach
    void setUp() {
      tile = new Tile(1); // A valid tile for testing setters/getters
    }

    @Test
    @DisplayName("setNext and getNext should work correctly")
    void testSetAndGetNext() {
      assertNull(tile.getNext(), "Next tile should initially be null");
      tile.setNext(mockNextTile);
      assertSame(mockNextTile, tile.getNext(), "getNext should return the set next tile");

      tile.setNext(null); // Test setting back to null
      assertNull(tile.getNext(), "Next tile should be null after setting to null");
    }

    @Test
    @DisplayName("setPrevious and getPrevious should work correctly")
    void testSetAndGetPrevious() {
      assertNull(tile.getPrevious(), "Previous tile should initially be null");
      tile.setPrevious(mockPreviousTile);
      assertSame(mockPreviousTile, tile.getPrevious(), "getPrevious should return the set previous tile");

      tile.setPrevious(null); // Test setting back to null
      assertNull(tile.getPrevious(), "Previous tile should be null after setting to null");
    }

    @Test
    @DisplayName("setAction and getAction should work correctly")
    void testSetAndGetAction() {
      assertNull(tile.getAction(), "Action should initially be null");
      tile.setAction(mockTileAction);
      assertSame(mockTileAction, tile.getAction(), "getAction should return the set action");

      tile.setAction(null); // Test setting back to null
      assertNull(tile.getAction(), "Action should be null after setting to null");
    }
  }


  @Nested
  @DisplayName("Land Method Tests")
  class LandMethodTests {
    private Tile tile;
    @Mock
    private TileAction mockAction;
    @Mock
    private Player mockPlayer; // Mocking Player as its internal state is not relevant for this test

    @BeforeEach
    void setUp() {
      tile = new Tile(5);
    }

    @Test
    @DisplayName("land() should do nothing if action is null")
    void testLand_whenActionIsNull_doesNothing() {
      tile.setAction(null); // Ensure action is null

      assertDoesNotThrow(() -> tile.land(mockPlayer), "Land method should not throw if action is null");
    }

    @Test
    @DisplayName("land() should call action.perform() if action is not null")
    void testLand_whenActionIsNotNull_actionPerformIsCalled() {
      tile.setAction(mockAction);
      tile.land(mockPlayer);

      // Verify that mockAction.perform(mockPlayer) was called exactly once
      verify(mockAction, times(1)).perform(mockPlayer);
    }

    @Test
    @DisplayName("land() should pass the correct player to action.perform()")
    void testLand_passesCorrectPlayerToActionPerform() {
      tile.setAction(mockAction);
      Player specificPlayer = mock(Player.class); // Use a fresh mock if needed for clarity
      tile.land(specificPlayer);

      verify(mockAction).perform(specificPlayer); // Verifies perform was called with specificPlayer
    }

    @Test
    @DisplayName("land() with null player should still call action.perform() with null player")
    void testLand_withNullPlayer_actionPerformIsCalledWithNullPlayer() {
      tile.setAction(mockAction);
      tile.land(null); // Passing null as player

      verify(mockAction, times(1)).perform(null);
    }
  }
}