package edu.ntnu.idi.bidata.model.actions.snakes;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
// edu.ntnu.idi.bidata.util.Logger is used implicitly by the SUT

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap; // Preserves insertion order, good for predictable iteration if needed
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchrodingerBoxActionTest {

  // For capturing System.out and System.err
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  private Player mockPlayer;
  private Board mockBoard;
  private Tile mockStartTile; // Will have ID 0
  private Tile mockEndTile;   // Will have ID 1 (for a 2-tile board setup)

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    // Mock Player
    mockPlayer = Mockito.mock(Player.class);
    when(mockPlayer.getName()).thenReturn("Tester");

    // Mock Tiles
    mockStartTile = Mockito.mock(Tile.class);
    when(mockStartTile.getId()).thenReturn(0);

    mockEndTile = Mockito.mock(Tile.class);
    when(mockEndTile.getId()).thenReturn(1); // Assuming a 2-tile board for simplicity

    // Mock Board
    mockBoard = Mockito.mock(Board.class);
    Map<Integer, Tile> tilesMap = new LinkedHashMap<>();
    tilesMap.put(0, mockStartTile);
    tilesMap.put(1, mockEndTile); // End tile is at index 1 for a 2-tile map

    when(mockBoard.getTiles()).thenReturn(tilesMap); // .size() will be 2
    when(mockBoard.getTile(0)).thenReturn(mockStartTile); // For startTile lookup
    when(mockBoard.getTile(1)).thenReturn(mockEndTile);   // For endTile lookup (size-1 = 1)
  }

  @AfterEach
  void restoreStreams() {
    // Restore original System.out and System.err
    System.setOut(originalOut);
    System.setErr(originalErr);

    outContent.reset();
    errContent.reset();
  }

  @Test
  @DisplayName("Default constructor should set default description")
  void testDefaultConstructor() {
    SchrodingerBoxAction action = new SchrodingerBoxAction();
    assertEquals("A mysterious Schrödinger's Box! Observe its contents or move on?", action.getDescription());
  }

  @Test
  @DisplayName("Constructor with description should set provided description")
  void testConstructor_WithDescription() {
    SchrodingerBoxAction action = new SchrodingerBoxAction("Custom Box of Wonders");
    assertEquals("Custom Box of Wonders", action.getDescription());
  }

  @Test
  @DisplayName("Constructor with null description should use default")
  void testConstructor_NullDescription() {
    SchrodingerBoxAction action = new SchrodingerBoxAction(null);
    assertEquals("A mysterious Schrödinger's Box! Observe its contents or move on?", action.getDescription());
  }

  @Test
  @DisplayName("Constructor with blank description should use default")
  void testConstructor_BlankDescription() {
    SchrodingerBoxAction action = new SchrodingerBoxAction("   ");
    assertEquals("A mysterious Schrödinger's Box! Observe its contents or move on?", action.getDescription());
  }

  @Test
  @DisplayName("perform should log player landing via Logger.info")
  void testPerform_LogsLanding() {
    SchrodingerBoxAction action = new SchrodingerBoxAction("Test Box");
    action.perform(mockPlayer);

    String logs = outContent.toString();
    assertTrue(logs.contains("[INFO]"), "Log should contain INFO level");
    assertTrue(logs.contains("Tester landed on: \"Test Box\". Awaiting decision via UI."), "Log message mismatch");
  }

  @Test
  @DisplayName("executeObserve should move player to start OR end tile and log outcome")
  void testExecuteObserve_MovesToStartOrEnd() {
    SchrodingerBoxAction action = new SchrodingerBoxAction();
    String outcomeMessage = action.executeObserve(mockPlayer, mockBoard);

    ArgumentCaptor<Tile> tileCaptor = ArgumentCaptor.forClass(Tile.class);
    verify(mockPlayer).setCurrentTile(tileCaptor.capture());
    Tile movedToTile = tileCaptor.getValue();

    String logs = outContent.toString(); // Capture all System.out logs

    boolean movedToStart = (movedToTile == mockStartTile);
    boolean movedToEnd = (movedToTile == mockEndTile);

    assertTrue(movedToStart || movedToEnd, "Player should have moved to either start or end tile.");

    if (movedToStart) {
      assertTrue(outcomeMessage.contains("Sent back to the start (Tile 0)!"), "Outcome message for start mismatch.");
      assertTrue(logs.contains("[INFO] Schrödinger outcome (Observe): Tester sent to tile 0 (START)."), "Log for start outcome mismatch.");
      assertTrue(logs.contains("[DEBUG] Player Tester moved to tile 0 due to Schrödinger Box (Observe)."), "Debug log for move to start mismatch.");
    } else { // Moved to End
      assertTrue(outcomeMessage.contains("Sent straight to the finish line (Tile 1)!"), "Outcome message for end mismatch.");
      assertTrue(logs.contains("[INFO] Schrödinger outcome (Observe): Tester sent to tile 1 (FINISH)."), "Log for end outcome mismatch.");
      assertTrue(logs.contains("[DEBUG] Player Tester moved to tile 1 due to Schrödinger Box (Observe)."), "Debug log for move to end mismatch.");
    }
  }

  @Test
  @DisplayName("executeObserve should handle null board and log error")
  void testExecuteObserve_NullBoard() {
    SchrodingerBoxAction action = new SchrodingerBoxAction();
    String outcome = action.executeObserve(mockPlayer, null);

    assertEquals("Tester opened the box, but the fabric of reality seems broken (board missing)!", outcome);
    verify(mockPlayer, never()).setCurrentTile(any());
    String errorLogs = errContent.toString();
    assertTrue(errorLogs.contains("[ERROR] Schrödinger (Observe) error: Board is null for player Tester"), "Error log mismatch");
  }

  @Test
  @DisplayName("executeObserve should handle empty board and log error")
  void testExecuteObserve_EmptyBoard() {
    when(mockBoard.getTiles()).thenReturn(java.util.Collections.emptyMap());
    SchrodingerBoxAction action = new SchrodingerBoxAction();
    String outcome = action.executeObserve(mockPlayer, mockBoard);

    assertEquals("Tester opened the box, but the universe is empty (no tiles on board)!", outcome);
    verify(mockPlayer, never()).setCurrentTile(any());
    String errorLogs = errContent.toString();
    assertTrue(errorLogs.contains("[ERROR] Schrödinger (Observe) error: Board has no tiles for player Tester"), "Error log mismatch");
  }

  @Test
  @DisplayName("executeObserve should handle null start tile and log error (if random directs to start)")
  void testExecuteObserve_NullStartTile() {
    // Make getTile(0) return null
    when(mockBoard.getTile(0)).thenReturn(null);
    // End tile is still present in the map for the other random path
    Map<Integer, Tile> tilesMap = new LinkedHashMap<>();
    // tilesMap.put(0, null); // Not strictly needed as getTile(0) is mocked directly
    tilesMap.put(1, mockEndTile); // End tile is still at ID 1
    when(mockBoard.getTiles()).thenReturn(tilesMap); // size is 1 (or 2 if we put null explicitly)


    SchrodingerBoxAction action = new SchrodingerBoxAction();

    String outcome = action.executeObserve(mockPlayer, mockBoard);
    String errorLogs = errContent.toString();
    String infoLogs = outContent.toString();


    if (outcome.contains("start point is missing")) {
      // This path means random tried to go to start, but it was null
      assertEquals("Tester opened the box, but the start point is missing from reality!", outcome);
      verify(mockPlayer, never()).setCurrentTile(any());
      assertTrue(errorLogs.contains("[ERROR] Schrödinger (Observe) error: Start tile (tile 0) is null."), "Error log for null start tile mismatch");
    } else {
      // This path means random tried to go to end, which should succeed
      verify(mockPlayer).setCurrentTile(mockEndTile);
      assertTrue(outcome.contains("Sent straight to the finish line (Tile 1)!"));
      assertTrue(infoLogs.contains("[INFO] Schrödinger outcome (Observe): Tester sent to tile 1 (FINISH)."));
    }
  }

  @Test
  @DisplayName("executeObserve should handle null end tile and log error (if random directs to end)")
  void testExecuteObserve_NullEndTile() {
    // Start tile is present and fine
    when(mockBoard.getTile(0)).thenReturn(mockStartTile);

    Map<Integer, Tile> tilesMap = new LinkedHashMap<>();
    tilesMap.put(0, mockStartTile); // Start tile at ID 0
    tilesMap.put(1, mockEndTile);   // Placeholder for end tile at ID 1, actual tile object doesn't matter here for size

    when(mockBoard.getTiles()).thenReturn(tilesMap); // So, board.getTiles().size() will be 2.
    // board.getTiles().size() - 1 will be 1.

    // Crucially, mock getTile(1) to return null
    when(mockBoard.getTile(1)).thenReturn(null); // This makes the "end tile" null when fetched by ID.

    SchrodingerBoxAction action = new SchrodingerBoxAction();
    String outcome = action.executeObserve(mockPlayer, mockBoard);
    String errorLogs = errContent.toString();
    String infoLogs = outContent.toString();

    if (outcome.contains("end point is missing")) {
      // This path means random tried to go to end, but it was null
      assertEquals("Tester opened the box, but the end point is missing from reality!", outcome);
      verify(mockPlayer, never()).setCurrentTile(any()); // Player should not move
      // Assert the exact error log content
      assertTrue(errorLogs.contains("[ERROR] Schrödinger (Observe) error: End tile (tile 1) is null."),
          "Error log for null end tile mismatch. Actual error log: " + errorLogs);
    } else {
      // This path means random tried to go to start, which should succeed
      verify(mockPlayer).setCurrentTile(mockStartTile);
      assertTrue(outcome.contains("Sent back to the start (Tile 0)!"));
      assertTrue(infoLogs.contains("[INFO] Schrödinger outcome (Observe): Tester sent to tile 0 (START)."));
    }
  }


  @Test
  @DisplayName("executeIgnore should not move player and log correctly via Logger.info")
  void testExecuteIgnore() {
    SchrodingerBoxAction action = new SchrodingerBoxAction("Ignorable Box of Mystery");
    String outcome = action.executeIgnore(mockPlayer);

    assertEquals("Tester cautiously decided to ignore: \"Ignorable Box of Mystery\"", outcome);
    verify(mockPlayer, never()).setCurrentTile(any()); // Player should not move

    String logs = outContent.toString();
    assertTrue(logs.contains("[INFO]"), "Log should contain INFO level");
    assertTrue(logs.contains("Tester chose to IGNORE the Schrödinger Box. No change in position."), "Log message for ignore mismatch");
  }
}