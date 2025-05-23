package edu.ntnu.idi.bidata.file;

import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.exception.JsonParseException;
import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.*;
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class BoardJsonReaderWriterTest {

  // For capturing Logger output
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  private String getOut() { return outContent.toString(); }
  private String getErr() { return errContent.toString(); }

  @Test
  @DisplayName("read throws JsonParseException for null reader")
  void read_NullReader_ThrowsJsonParseException() {
    JsonParseException ex = assertThrows(JsonParseException.class,
        () -> BoardJsonReaderWriter.read(null, GameVariant.SNAKES_LADDERS));
    assertTrue(ex.getMessage().contains("Reader cannot be null"));
  }

  @Test
  @DisplayName("read throws JsonParseException for invalid JSON syntax")
  void read_InvalidJsonSyntax_ThrowsJsonParseException() {
    String invalidJson = "{ \"tiles\": [ {\"id\":0, \"nextId\":1 }"; // Missing closing ] and }
    StringReader reader = new StringReader(invalidJson);
    assertThrows(JsonParseException.class,
        () -> BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS));
  }

  @Test
  @DisplayName("read throws JsonParseException if 'tiles' array is missing")
  void read_MissingTilesArray_ThrowsJsonParseExceptionOrNPE() {
    String jsonWithoutTiles = "{\"description\": \"A board without tiles\"}";
    StringReader reader = new StringReader(jsonWithoutTiles);

    assertThrows(AssertionError.class,
        () -> BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS));
  }


  @Test
  @DisplayName("read successfully creates tiles in first pass")
  void read_FirstPass_CreatesTiles() {
    String json = "{\"tiles\": [{\"id\":0}, {\"id\":1}, {\"id\":5}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);

    assertNotNull(board.getTile(0));
    assertNotNull(board.getTile(1));
    assertNotNull(board.getTile(5));
    assertEquals(3, board.getTiles().size());
    assertTrue(getOut().contains("Created and added tile with id: 0"));
    assertTrue(getOut().contains("Created and added tile with id: 1"));
    assertTrue(getOut().contains("Created and added tile with id: 5"));
  }

  @Test
  @DisplayName("read sets next and previous links correctly for SNAKES_LADDERS")
  void read_SecondPass_SetsLinks_SnakesLadders() {
    String json = "{\"tiles\": [" +
        "{\"id\":0, \"nextId\":1}," +
        "{\"id\":1, \"nextId\":2}," +
        "{\"id\":2}" +
        "]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);

    Tile tile0 = board.getTile(0);
    Tile tile1 = board.getTile(1);
    Tile tile2 = board.getTile(2);

    assertNotNull(tile0);
    assertNotNull(tile1);
    assertNotNull(tile2);

    assertEquals(tile1, tile0.getNext(), "Tile 0 next should be Tile 1");
    assertNull(tile0.getPrevious(), "Tile 0 previous should be null");

    assertEquals(tile2, tile1.getNext(), "Tile 1 next should be Tile 2");
    assertEquals(tile0, tile1.getPrevious(), "Tile 1 previous should be Tile 0");

    assertNull(tile2.getNext(), "Tile 2 next should be null");
    assertEquals(tile1, tile2.getPrevious(), "Tile 2 previous should be Tile 1");

    assertTrue(getOut().contains("Set next link for tile 0 to tile 1"));
    assertTrue(getOut().contains("Set previous link for tile 1 to tile 0"));
  }

  @Test
  @DisplayName("read handles nextId pointing to a non-existent tile")
  void read_NextIdNotFound_LogsWarning() {
    String json = "{\"tiles\": [{\"id\":0, \"nextId\":99}]}"; // Tile 99 does not exist
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);

    Tile tile0 = board.getTile(0);
    assertNotNull(tile0);
    assertNull(tile0.getNext(), "Next link should not be set if next tile not found.");
    assertTrue(getErr().contains("Next tile with id 99 not found for tile 0. Link not set."));
  }

  // --- SNAKES_LADDERS Action Tests ---
  @Test
  @DisplayName("read sets LadderAction correctly for SNAKES_LADDERS")
  void read_SnakesLadders_LadderAction() {
    String json = "{\"tiles\": [{\"id\":0, \"action\":{\"type\":\"LadderAction\", \"description\":\"Go up!\", \"steps\":5}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);
    Tile tile0 = board.getTile(0);
    assertNotNull(tile0.getAction());
    assertInstanceOf(LadderAction.class, tile0.getAction());
    assertEquals(5, ((LadderAction) tile0.getAction()).getSteps());
    assertTrue(getOut().contains("Set LadderAction for tile 0 with 5 steps."));
  }

  @Test
  @DisplayName("read sets SnakeAction correctly for SNAKES_LADDERS")
  void read_SnakesLadders_SnakeAction() {
    String json = "{\"tiles\": [{\"id\":1, \"action\":{\"type\":\"SnakeAction\", \"description\":\"Slide down!\", \"steps\":3}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);
    Tile tile1 = board.getTile(1);
    assertNotNull(tile1.getAction());
    assertInstanceOf(SnakeAction.class, tile1.getAction());
    assertEquals(3, ((SnakeAction) tile1.getAction()).getSteps());
  }

  @Test
  @DisplayName("read sets SchrodingerBoxAction correctly for SNAKES_LADDERS")
  void read_SnakesLadders_SchrodingerBoxAction() {
    String json = "{\"tiles\": [{\"id\":2, \"action\":{\"type\":\"SchrodingerBoxAction\", \"description\":\"Mystery!\"}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);
    Tile tile2 = board.getTile(2);
    assertNotNull(tile2.getAction());
    assertInstanceOf(SchrodingerBoxAction.class, tile2.getAction());
    assertEquals("Mystery!", ((SchrodingerBoxAction) tile2.getAction()).getDescription());
  }

  @Test
  @DisplayName("read handles unknown action type for SNAKES_LADDERS")
  void read_SnakesLadders_UnknownActionType() {
    String json = "{\"tiles\": [{\"id\":0, \"action\":{\"type\":\"FlyAction\"}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);
    Tile tile0 = board.getTile(0);
    assertNull(tile0.getAction());
    assertTrue(getErr().contains("Unknown Snakes & Ladders action type 'FlyAction' for tile 0. No action set."));
  }

  // --- MINI_MONOPOLY Action Tests ---
  @Test
  @DisplayName("read sets GoAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_GoAction() {
    String json = "{\"tiles\": [{\"id\":0, \"action\":{\"type\":\"GoAction\", \"description\":\"Collect Salary\", \"reward\":200}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile0 = board.getTile(0);
    assertNotNull(tile0.getAction());
    assertInstanceOf(GoAction.class, tile0.getAction());
    // GoAction doesn't have getters for reward/description, verify by log
    assertTrue(getOut().contains("Set GoAction for tile 0 with reward 200"));
  }

  @Test
  @DisplayName("read sets PropertyAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_PropertyAction() {
    String json = "{\"tiles\": [{\"id\":1, \"action\":{\"type\":\"PropertyAction\", \"name\":\"Boardwalk\", \"cost\":400, \"rent\":50, \"colorGroup\":\"Blue\"}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile1 = board.getTile(1);
    assertNotNull(tile1.getAction());
      assertInstanceOf(PropertyAction.class, tile1.getAction());
    PropertyAction pa = (PropertyAction) tile1.getAction();
    assertEquals("Boardwalk", pa.getName());
    assertEquals(400, pa.getCost());
    assertEquals(50, pa.getRent());
    assertEquals("Blue", pa.getColorGroup());
  }

  @Test
  @DisplayName("read sets CommunityChestAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_CommunityChestAction() {
    String json = "{\"tiles\": [{\"id\":2, \"action\":{\"type\":\"CommunityChestAction\", \"description\":\"CC\"}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile2 = board.getTile(2);
    assertNotNull(tile2.getAction());
      assertInstanceOf(CommunityChestAction.class, tile2.getAction());
    assertEquals("CC", ((CommunityChestAction) tile2.getAction()).getDescription());
  }

  @Test
  @DisplayName("read sets TaxAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_TaxAction() {
    String json = "{\"tiles\": [{\"id\":3, \"action\":{\"type\":\"TaxAction\", \"description\":\"Income Tax\", \"amount\":200}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile3 = board.getTile(3);
    assertNotNull(tile3.getAction());
      assertInstanceOf(TaxAction.class, tile3.getAction());
    assertEquals(200, ((TaxAction) tile3.getAction()).getTaxAmount());
  }

  @Test
  @DisplayName("read sets RailroadAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_RailroadAction() {
    String json = "{\"tiles\": [{\"id\":4, \"action\":{\"type\":\"RailroadAction\", \"name\":\"Reading RR\", \"cost\":200, \"rent\":25}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile4 = board.getTile(4);
    assertNotNull(tile4.getAction());
      assertInstanceOf(RailroadAction.class, tile4.getAction());
    assertEquals("Reading RR", ((RailroadAction)tile4.getAction()).getName());
  }

  @Test
  @DisplayName("read sets UtilityAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_UtilityAction() {
    String json = "{\"tiles\": [{\"id\":5, \"action\":{\"type\":\"UtilityAction\", \"name\":\"Water Works\", \"cost\":150}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile5 = board.getTile(5);
    assertNotNull(tile5.getAction());
      assertInstanceOf(UtilityAction.class, tile5.getAction());
    assertEquals("Water Works", ((UtilityAction)tile5.getAction()).getName());
  }

  @Test
  @DisplayName("read sets ChanceAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_ChanceAction() {
    String json = "{\"tiles\": [{\"id\":6, \"action\":{\"type\":\"ChanceAction\", \"description\":\"Chance\"}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile6 = board.getTile(6);
    assertNotNull(tile6.getAction());
      assertInstanceOf(ChanceAction.class, tile6.getAction());
    assertEquals("Chance", ((ChanceAction) tile6.getAction()).getDescription());
  }

  @Test
  @DisplayName("read sets JailAction (Just Visiting) correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_JailAction() {
    String json = "{\"tiles\": [{\"id\":7, \"action\":{\"type\":\"JailAction\", \"description\":\"Visiting\"}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile7 = board.getTile(7);
    assertNotNull(tile7.getAction());
      assertInstanceOf(JailAction.class, tile7.getAction());
    // JailAction has no getters, check log
    assertTrue(getOut().contains("Set JailAction (Just Visiting) for tile " + 7));
  }

  @Test
  @DisplayName("read sets GoToJailAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_GoToJailAction() {
    String json = "{\"tiles\": [{\"id\":8, \"action\":{\"type\":\"GoToJailAction\", \"description\":\"Go To Jail!\", \"targetId\":20}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile8 = board.getTile(8);
    assertNotNull(tile8.getAction());
      assertInstanceOf(GoToJailAction.class, tile8.getAction());
    // GoToJailAction has no getters for targetId/description, check log
    assertTrue(getOut().contains("Set GoToJailAction for tile " + 8 + ", targeting jail tile 20"));
  }

  @Test
  @DisplayName("read sets FreeParkingAction correctly for MINI_MONOPOLY")
  void read_MiniMonopoly_FreeParkingAction() {
    String json = "{\"tiles\": [{\"id\":9, \"action\":{\"type\":\"FreeParkingAction\", \"description\":\"Free Parking\"}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile9 = board.getTile(9);
    assertNotNull(tile9.getAction());
      assertInstanceOf(FreeParkingAction.class, tile9.getAction());
    // FreeParkingAction has no getters, check log
    assertTrue(getOut().contains("Set FreeParkingAction for tile " + 9));
  }

  @Test
  @DisplayName("read handles unknown action type for MINI_MONOPOLY")
  void read_MiniMonopoly_UnknownActionType() {
    String json = "{\"tiles\": [{\"id\":0, \"action\":{\"type\":\"CollectBonusAction\"}}]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.MINI_MONOPOLY);
    Tile tile0 = board.getTile(0);
    assertNull(tile0.getAction());
    assertTrue(getErr().contains("Unknown Monopoly action type 'CollectBonusAction' for tile 0. No action set."));
  }

  @Test
  @DisplayName("read handles missing optional description for action")
  void read_ActionMissingDescription_UsesEmptyString() {
    // SchrodingerBoxAction constructor handles null/empty description and defaults.
    String json = "{\"tiles\": [{\"id\":0, \"action\":{\"type\":\"SchrodingerBoxAction\"}}]}"; // No description field
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);
    Tile tile0 = board.getTile(0);
    assertNotNull(tile0.getAction());
    assertInstanceOf(SchrodingerBoxAction.class, tile0.getAction());
    // SchrodingerBoxAction's default constructor or one with "" will give a default desc.
    assertFalse(((SchrodingerBoxAction)tile0.getAction()).getDescription().isEmpty());
  }

  @Test
  @DisplayName("read handles existing previous link (does not overwrite)")
  void read_SecondPass_ExistingPreviousLink_DoesNotOverwrite() {
    String json = "{\"tiles\": [" +
        "{\"id\":0, \"nextId\":1}," +
        "{\"id\":1, \"nextId\":2}," + // T1->prev = T0
        "{\"id\":3, \"nextId\":1}" +  // T3 tries to set T1->prev = T3, should be ignored if T1->prev already T0
        "]}";
    StringReader reader = new StringReader(json);
    Board board = BoardJsonReaderWriter.read(reader, GameVariant.SNAKES_LADDERS);

    Tile tile0 = board.getTile(0);
    Tile tile1 = board.getTile(1);
    assertNotNull(tile1);
    assertEquals(tile0, tile1.getPrevious(), "Tile 1 previous should remain Tile 0");
    assertTrue(getOut().contains("Previous link for tile 1 was already set to 0. Not overwriting from tile 3"));
  }
}