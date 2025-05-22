package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.actions.TileAction; // Assuming this import might be needed if TileAction was used directly
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// If you decide to use lenient stubs for a specific nested class:
// import org.mockito.junit.jupiter.MockitoSettings;
// import org.mockito.quality.Strictness;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerTest {

  @Mock
  private Tile mockStartTile;
  @Mock
  private Tile mockNextTile1;
  @Mock
  private Tile mockNextTile2;

  private Player player;

  // Common valid inputs
  private final String VALID_NAME = "Alice";
  private final String VALID_PIECE = "Hat";


  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Constructor (name, start) uses default piece and null money")
    void testConstructor_nameStart_usesDefaults() {
      player = new Player(VALID_NAME, mockStartTile);
      assertEquals(VALID_NAME, player.getName());
      assertSame(mockStartTile, player.getCurrentTile());
      assertEquals(Player.DEFAULT_PIECE_IDENTIFIER, player.getPieceIdentifier());
      assertEquals(0, player.getMoney(), "Default money should be 0 (as per getMoney behavior for null)");
    }

    @Test
    @DisplayName("Constructor (name, start, piece) uses specified piece and null money")
    void testConstructor_nameStartPiece_usesSpecifiedPiece() {
      player = new Player(VALID_NAME, mockStartTile, VALID_PIECE);
      assertEquals(VALID_NAME, player.getName());
      assertSame(mockStartTile, player.getCurrentTile());
      assertEquals(VALID_PIECE, player.getPieceIdentifier());
      assertEquals(0, player.getMoney());
    }

    @Test
    @DisplayName("Constructor (name, start, piece, money) sets all values")
    void testConstructor_allArgs_setsAllValues() {
      Integer initialMoney = 1500;
      player = new Player(VALID_NAME, mockStartTile, VALID_PIECE, initialMoney);
      assertEquals(VALID_NAME, player.getName());
      assertSame(mockStartTile, player.getCurrentTile());
      assertEquals(VALID_PIECE, player.getPieceIdentifier());
      assertEquals(initialMoney, player.getMoney());
    }

    @Test
    @DisplayName("Constructor with null money sets money to null internally")
    void testConstructor_allArgs_withNullMoney() {
      player = new Player(VALID_NAME, mockStartTile, VALID_PIECE, null);
      assertEquals(0, player.getMoney(), "getMoney should return 0 when internal money is null");
    }

    @Test
    @DisplayName("Constructor with negative money (allowed by constructor, checked by setMoney)")
    void testConstructor_allArgs_withNegativeMoney() {
      Integer initialMoney = -100;
      player = new Player(VALID_NAME, mockStartTile, VALID_PIECE, initialMoney);
      assertEquals(initialMoney.intValue(), player.getMoney(), "Constructor should allow negative money if passed directly.");
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  \t  "})
    @DisplayName("Constructor throws InvalidParameterException for null or blank name")
    void testConstructor_invalidName_throwsException(String invalidName) {
      InvalidParameterException ex = assertThrows(InvalidParameterException.class,
          () -> new Player(invalidName, mockStartTile));
      assertEquals("Player name must not be empty", ex.getMessage());
    }

    @Test
    @DisplayName("Constructor throws InvalidParameterException for null start tile")
    void testConstructor_nullStartTile_throwsException() {
      InvalidParameterException ex = assertThrows(InvalidParameterException.class,
          () -> new Player(VALID_NAME, null));
      assertEquals("Starting tile must not be null", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  \t  "})
    @DisplayName("Constructor uses default piece identifier for null or blank input")
    void testConstructor_nullOrBlankPiece_usesDefault(String pieceInput) {
      player = new Player(VALID_NAME, mockStartTile, pieceInput, 100);
      assertEquals(Player.DEFAULT_PIECE_IDENTIFIER, player.getPieceIdentifier());
    }

    @Test
    @DisplayName("Constructor trims piece identifier")
    void testConstructor_trimsPieceIdentifier() {
      player = new Player(VALID_NAME, mockStartTile, "  MyPiece  ", 100);
      assertEquals("MyPiece", player.getPieceIdentifier());
    }
  }

  @Nested
  @DisplayName("Tile Setter/Getter Tests")
  class TileSetterGetterTests {
    @BeforeEach
    void setupPlayer() {
      player = new Player(VALID_NAME, mockStartTile);
    }

    @Test
    @DisplayName("setCurrentTile and getCurrentTile work correctly")
    void testSetAndGetCurrentTile() {
      assertSame(mockStartTile, player.getCurrentTile());
      player.setCurrentTile(mockNextTile1);
      assertSame(mockNextTile1, player.getCurrentTile());
    }

    @Test
    @DisplayName("setCurrentTile with null throws InvalidParameterException")
    void testSetCurrentTile_null_throwsException() {
      InvalidParameterException ex = assertThrows(InvalidParameterException.class,
          () -> player.setCurrentTile(null));
      assertEquals("Current tile must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("setTile works correctly")
    void testSetTile() {
      player.setTile(mockNextTile1);
      assertSame(mockNextTile1, player.getCurrentTile());
    }

    @Test
    @DisplayName("setTile with null throws InvalidParameterException")
    void testSetTile_null_throwsException() {
      InvalidParameterException ex = assertThrows(InvalidParameterException.class,
          () -> player.setTile(null));
      assertEquals("Tile must not be null", ex.getMessage());
    }
  }

  @Nested
  @DisplayName("Move Method Tests")
  class MoveMethodTests {

    @BeforeEach
    void setupPlayer() {
      player = new Player(VALID_NAME, mockStartTile);
    }

    @Test
    @DisplayName("move with 0 steps does not change tile and calls land")
    void testMove_zeroSteps() {
      // No tile linking needed as player doesn't move.
      player.move(0);
      assertSame(mockStartTile, player.getCurrentTile(), "Player should not move for 0 steps.");
      verify(mockStartTile, times(1)).land(player);
    }

    @Test
    @DisplayName("move forward positive steps within bounds")
    void testMove_forwardPositiveSteps() {
      when(mockStartTile.getNext()).thenReturn(mockNextTile1);
      when(mockNextTile1.getNext()).thenReturn(mockNextTile2);

      player.move(1);
      assertSame(mockNextTile1, player.getCurrentTile());
      verify(mockNextTile1, times(1)).land(player);

      // Reset player to start tile for the next part of the test
      player.setCurrentTile(mockStartTile);
      clearInvocations(mockStartTile, mockNextTile1, mockNextTile2); // Clear land calls for clean verification

      player.move(2);
      assertSame(mockNextTile2, player.getCurrentTile());
      verify(mockNextTile2, times(1)).land(player);
    }

    @Test
    @DisplayName("move backward negative steps within bounds")
    void testMove_backwardNegativeSteps() {
      player.setCurrentTile(mockNextTile2); // Start at a tile that allows backward movement

      when(mockNextTile2.getPrevious()).thenReturn(mockNextTile1);
      when(mockNextTile1.getPrevious()).thenReturn(mockStartTile);

      player.move(-1);
      assertSame(mockNextTile1, player.getCurrentTile());
      verify(mockNextTile1, times(1)).land(player);

      // Reset player to a suitable tile for the next part of the test
      player.setCurrentTile(mockNextTile2);
      clearInvocations(mockStartTile, mockNextTile1, mockNextTile2); // Clear land calls

      player.move(-2);
      assertSame(mockStartTile, player.getCurrentTile());
      verify(mockStartTile, times(1)).land(player);
    }

    @Test
    @DisplayName("move forward hits end of board (null next)")
    void testMove_forwardHitsEndOfBoard() {
      when(mockStartTile.getNext()).thenReturn(mockNextTile1);
      when(mockNextTile1.getNext()).thenReturn(null); // mockNextTile1 is the last tile

      player.move(2); // Attempt to move 2 steps from mockStartTile
      assertSame(mockNextTile1, player.getCurrentTile(), "Should stop at the last available tile.");
      verify(mockNextTile1, times(1)).land(player);
    }

    @Test
    @DisplayName("move backward hits start of board (null previous)")
    void testMove_backwardHitsStartOfBoard() {
      player.setCurrentTile(mockNextTile1); // Start from a tile that allows backward movement

      when(mockNextTile1.getPrevious()).thenReturn(mockStartTile);
      when(mockStartTile.getPrevious()).thenReturn(null); // mockStartTile is the first tile going backwards

      player.move(-2); // Attempt to move 2 steps back from mockNextTile1
      assertSame(mockStartTile, player.getCurrentTile(), "Should stop at the first available tile (going backward).");
      verify(mockStartTile, times(1)).land(player);
    }

    @Test
    @DisplayName("move calls land() on the final tile")
    void testMove_callsLandOnFinalTile() {
      when(mockStartTile.getNext()).thenReturn(mockNextTile1);

      player.move(1); // Moves to mockNextTile1
      verify(mockNextTile1, times(1)).land(player);
      verify(mockStartTile, never()).land(player); // Should not land on the start tile again if moved
    }

    @Test
    @DisplayName("move when currentTile.land() is called - verifies interaction")
    void testMove_landInteraction() {

      when(mockStartTile.getNext()).thenReturn(mockNextTile1);

      player.move(1);

      assertSame(mockNextTile1, player.getCurrentTile());
      verify(mockNextTile1).land(player); // Crucial verification: land was called on the destination.
    }
  }


  @Nested
  @DisplayName("Money Management Tests")
  class MoneyManagementTests {

    @BeforeEach
    void setupPlayer() {
      player = new Player(VALID_NAME, mockStartTile); // Starts with money = null
    }

    @Test
    @DisplayName("getMoney returns 0 when money is null")
    void testGetMoney_whenNull_returnsZero() {
      assertEquals(0, player.getMoney());
    }

    @Test
    @DisplayName("setMoney with positive value updates money")
    void testSetMoney_positiveValue() {
      player.setMoney(100);
      assertEquals(100, player.getMoney());
    }

    @Test
    @DisplayName("setMoney with zero updates money")
    void testSetMoney_zeroValue() {
      player.setMoney(0);
      assertEquals(0, player.getMoney());
    }

    @Test
    @DisplayName("setMoney with zero updates money to zero (getMoney returns 0)")
    void testSetMoney_nullValue() {
      player.setMoney(100); // Set to non-zero first
      player.setMoney(0);
      assertEquals(0, player.getMoney()); // Observable behavior
    }

    @Test
    @DisplayName("setMoney with negative value throws InvalidParameterException")
    void testSetMoney_negativeValue_throwsException() {
      InvalidParameterException ex = assertThrows(InvalidParameterException.class,
          () -> player.setMoney(-50));
      assertEquals("Money must not be negative", ex.getMessage());
    }

    @Test
    @DisplayName("increaseMoney when money is null initializes to 0 then increases")
    void testIncreaseMoney_whenNull() {
      player.increaseMoney(50);
      assertEquals(50, player.getMoney());
    }

    @Test
    @DisplayName("increaseMoney when money is not null adds to existing money")
    void testIncreaseMoney_whenNotNull() {
      player.setMoney(100);
      player.increaseMoney(50);
      assertEquals(150, player.getMoney());
    }

    @Test
    @DisplayName("increaseMoney with 0 amount does not change money")
    void testIncreaseMoney_withZeroAmount() {
      player.setMoney(100);
      player.increaseMoney(0);
      assertEquals(100, player.getMoney());
    }

    @Test
    @DisplayName("increaseMoney with negative amount current behavior")
    void testIncreaseMoney_withNegativeAmount() {
      player.setMoney(100);
      player.increaseMoney(-10);
      assertEquals(90, player.getMoney(), "Current impl adds negative amount.");
      // Consider if this should throw an exception for negative amounts
    }

    @Test
    @DisplayName("decreaseMoney when money is sufficient")
    void testDecreaseMoney_sufficientFunds() {
      player.setMoney(100);
      player.decreaseMoney(30);
      assertEquals(70, player.getMoney());
    }

    @Test
    @DisplayName("decreaseMoney to exactly zero")
    void testDecreaseMoney_toZero() {
      player.setMoney(100);
      player.decreaseMoney(100);
      assertEquals(0, player.getMoney());
    }

    @Test
    @DisplayName("decreaseMoney when money is null throws InvalidParameterException")
    void testDecreaseMoney_whenNull_throwsException() {
      InvalidParameterException ex = assertThrows(InvalidParameterException.class,
          () -> player.decreaseMoney(10));
      assertEquals("Not enough money or money not initialized.", ex.getMessage());
    }

    @Test
    @DisplayName("decreaseMoney with insufficient funds throws InvalidParameterException")
    void testDecreaseMoney_insufficientFunds_throwsException() {
      player.setMoney(50);
      InvalidParameterException ex = assertThrows(InvalidParameterException.class,
          () -> player.decreaseMoney(60));
      assertEquals("Not enough money or money not initialized.", ex.getMessage());
    }

    @Test
    @DisplayName("decreaseMoney with 0 amount does not change money")
    void testDecreaseMoney_withZeroAmount() {
      player.setMoney(100);
      player.decreaseMoney(0);
      assertEquals(100, player.getMoney());
    }

    @Test
    @DisplayName("decreaseMoney with negative amount current behavior")
    void testDecreaseMoney_withNegativeAmount() {
      player.setMoney(100);
      player.decreaseMoney(-10);
      assertEquals(110, player.getMoney(), "Current impl subtracts negative amount.");
      // Consider if this should throw an exception for negative amounts
    }
  }

  @Nested
  @DisplayName("Stubbed Method Tests")
  class StubbedMethodTests {

    @BeforeEach
    void setupPlayer() {
      player = new Player(VALID_NAME, mockStartTile);
    }

    @Test
    @DisplayName("getLastDiceRoll currently returns 0")
    void testGetLastDiceRoll() {
      assertEquals(0, player.getLastDiceRoll());
    }

    @Test
    @DisplayName("getUtilitiesOwnedCount currently returns 0")
    void testGetUtilitiesOwnedCount() {
      assertEquals(0, player.getUtilitiesOwnedCount());
    }
  }

  @Nested
  @DisplayName("Getter Tests for Final Fields")
  class FinalFieldGetterTests {

    @Test
    @DisplayName("getName returns the correct player name")
    void testGetName() {
      player = new Player("Bob", mockStartTile);
      assertEquals("Bob", player.getName());
    }

    @Test
    @DisplayName("getPieceIdentifier returns the correct piece identifier")
    void testGetPieceIdentifier() {
      player = new Player("Carol", mockStartTile, "Car");
      assertEquals("Car", player.getPieceIdentifier());
    }
  }
}