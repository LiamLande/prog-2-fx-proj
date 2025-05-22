package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
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

class GoToJailActionTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  private Player mockPlayer;
  private Tile mockCurrentTile;
  private Tile mockJailTile;
  private Tile mockIntermediateTile1;
  private MonopolyService mockMonopolyService;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));

    mockPlayer = mock(Player.class);
    mockCurrentTile = mock(Tile.class);
    mockJailTile = mock(Tile.class);
    mockIntermediateTile1 = mock(Tile.class);
    mockMonopolyService = mock(MonopolyService.class);

    when(mockPlayer.getCurrentTile()).thenReturn(mockCurrentTile);
    ServiceLocator.setMonopolyService(mockMonopolyService);
  }

  @AfterEach
  void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  @DisplayName("Constructor should initialize with valid parameters")
  void testConstructor_Valid() {
    assertDoesNotThrow(() -> new GoToJailAction("Go to Jail!", 10));
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for null description")
  void testConstructor_NullDescription() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new GoToJailAction(null, 10));
    assertEquals("GoToJailAction description must not be empty", e.getMessage());
  }

  @Test
  @DisplayName("Constructor should throw InvalidParameterException for blank description")
  void testConstructor_BlankDescription() {
    Exception e = assertThrows(InvalidParameterException.class,
        () -> new GoToJailAction("  ", 10));
    assertEquals("GoToJailAction description must not be empty", e.getMessage());
  }

  @Test
  @DisplayName("perform should move player to jail tile (found forwards)")
  void testPerform_JailFoundForwards() {
    int jailId = 10;
    when(mockCurrentTile.getId()).thenReturn(5);
    when(mockCurrentTile.getNext()).thenReturn(mockIntermediateTile1);
    when(mockIntermediateTile1.getId()).thenReturn(6);
    when(mockIntermediateTile1.getNext()).thenReturn(mockJailTile);
    when(mockJailTile.getId()).thenReturn(jailId); // Jail found

    GoToJailAction action = new GoToJailAction("Go Directly to Jail", jailId);
    action.perform(mockPlayer);

    verify(mockMonopolyService).sendToJail(mockPlayer);
    assertTrue(outContent.toString().contains("Go Directly to Jail"));
  }

  @Test
  @DisplayName("perform should move player to jail tile (found backwards)")
  void testPerform_JailFoundBackwards() {
    int jailId = 2;
    when(mockCurrentTile.getId()).thenReturn(5);
    when(mockCurrentTile.getPrevious()).thenReturn(mockIntermediateTile1);
    when(mockIntermediateTile1.getId()).thenReturn(4);
    when(mockIntermediateTile1.getPrevious()).thenReturn(mockJailTile);
    when(mockJailTile.getId()).thenReturn(jailId); // Jail found

    // Mock next path to ensure backward path is taken
    when(mockCurrentTile.getNext()).thenReturn(null); // Or a long path that doesn't find jail quickly

    GoToJailAction action = new GoToJailAction("Oops, Jail Time!", jailId);
    action.perform(mockPlayer);

    // Expected steps: from 5 to 2 via 4 => (4-5) + (2-4) -> simplified to -2 direct steps
    verify(mockPlayer).move(-2);
    verify(mockMonopolyService).sendToJail(mockPlayer);
    assertTrue(outContent.toString().contains("Oops, Jail Time!"));
  }

  @Test
  @DisplayName("perform should handle jail tile not found (hitting safety break in calculateSteps)")
  void testPerform_JailNotFound_HitsSafetyBreak() {
    int jailId = 100; // A jail ID that won't be found
    final int MAX_SEARCH_PATH_BEFORE_NULL = 5; // Make the path before null shorter than safety break (40)

    when(mockCurrentTile.getId()).thenReturn(1);
    when(mockPlayer.getCurrentTile()).thenReturn(mockCurrentTile); // Make sure player starts at mockCurrentTile

    // Setup a forward path that eventually becomes null
    Tile lastForwardReachableTile = mockCurrentTile;
    for (int i = 1; i <= MAX_SEARCH_PATH_BEFORE_NULL; i++) {
      Tile nextMock = mock(Tile.class);
      when(nextMock.getId()).thenReturn(1 + i); // Different IDs
      when(lastForwardReachableTile.getNext()).thenReturn(nextMock);
      lastForwardReachableTile = nextMock;
    }
    when(lastForwardReachableTile.getNext()).thenReturn(null); // End of forward path

    // Setup a backward path that eventually becomes null
    Tile lastBackwardReachableTile = mockCurrentTile;
    for (int i = 1; i <= MAX_SEARCH_PATH_BEFORE_NULL; i++) {
      Tile prevMock = mock(Tile.class);
      when(prevMock.getId()).thenReturn(1 - i); // Different IDs
      when(lastBackwardReachableTile.getPrevious()).thenReturn(prevMock);
      lastBackwardReachableTile = prevMock;
    }
    when(lastBackwardReachableTile.getPrevious()).thenReturn(null); // End of backward path

    GoToJailAction action = new GoToJailAction("Lost way to Jail", jailId);

    action.perform(mockPlayer);

    verify(mockPlayer, never()).move(anyInt());
    verify(mockMonopolyService, never()).sendToJail(mockPlayer);
    assertTrue(outContent.toString().contains("Lost way to Jail"));
  }

  @Test
  @DisplayName("perform should handle current tile being the jail tile")
  void testPerform_CurrentIsJail() {
    int jailId = 5;
    when(mockCurrentTile.getId()).thenReturn(jailId); // Player is already on jail tile

    GoToJailAction action = new GoToJailAction("Already at Jail", jailId);
    action.perform(mockPlayer);

    verify(mockPlayer).move(0); // Moves 0 steps
    verify(mockMonopolyService).sendToJail(mockPlayer);
    assertTrue(outContent.toString().contains("Already at Jail"));
  }

  @Test
  @DisplayName("perform should handle null monopoly service from ServiceLocator")
  void testPerform_NullMonopolyService() {
    ServiceLocator.setMonopolyService(null); // Set service to null

    int jailId = 10;
    when(mockCurrentTile.getId()).thenReturn(5);
    when(mockCurrentTile.getNext()).thenReturn(mockJailTile);
    when(mockJailTile.getId()).thenReturn(jailId);

    GoToJailAction action = new GoToJailAction("Go to Jail, no service", jailId);
    // Should not throw NPE, but service call won't happen
    assertDoesNotThrow(() -> action.perform(mockPlayer));

    verify(mockPlayer).move(1);
    // mockMonopolyService is null, so sendToJail on it won't be verified (it would NPE if called)
    assertTrue(outContent.toString().contains("Go to Jail, no service"));
  }
}