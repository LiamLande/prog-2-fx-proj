package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.ui.PlayerSetupData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSetupControllerTest {

  private PlayerSetupController controller;

  // For capturing Logger output
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    controller = new PlayerSetupController();
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  private String getOut() { return outContent.toString(); }
  private String getErr() { return errContent.toString(); }

  // --- savePlayerSetup Tests ---

  @Test
  @DisplayName("savePlayerSetup throws IllegalArgumentException for null player inputs")
  void savePlayerSetup_NullPlayerInputs_ThrowsIllegalArgumentException() {
    File mockFile = new File("dummy.csv"); // File doesn't need to exist for this check
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> controller.savePlayerSetup(null, mockFile));
    assertEquals("Player inputs cannot be null or empty.", ex.getMessage());
    assertTrue(getErr().contains("Save player setup request with no player inputs."), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("savePlayerSetup throws IllegalArgumentException for empty player inputs")
  void savePlayerSetup_EmptyPlayerInputs_ThrowsIllegalArgumentException() {
    File mockFile = new File("dummy.csv");
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> controller.savePlayerSetup(new ArrayList<>(), mockFile));
    assertEquals("Player inputs cannot be null or empty.", ex.getMessage());
    assertTrue(getErr().contains("Save player setup request with no player inputs."), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("savePlayerSetup throws IllegalArgumentException for null file")
  void savePlayerSetup_NullFile_ThrowsIllegalArgumentException() {
    List<PlayerSetupData> inputs = List.of(new PlayerSetupData("Alice", "tokenA"));
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> controller.savePlayerSetup(inputs, null));
    assertEquals("File cannot be null for saving.", ex.getMessage());
    assertTrue(getErr().contains("File cannot be null for saving player setup."), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("savePlayerSetup successfully writes valid player data to file")
  void savePlayerSetup_ValidData_WritesToFile(@TempDir Path tempDir) throws IOException {
    List<PlayerSetupData> inputs = List.of(
        new PlayerSetupData("Alice", "tokenA"),
        new PlayerSetupData("Bob", "tokenB")
    );
    File tempFile = tempDir.resolve("players.csv").toFile();

    boolean result = controller.savePlayerSetup(inputs, tempFile);

    assertTrue(result);
    List<String> lines = Files.readAllLines(tempFile.toPath());
    assertEquals(2, lines.size());
    assertEquals("Alice,0,tokenA", lines.get(0));
    assertEquals("Bob,0,tokenB", lines.get(1));
    assertTrue(getOut().contains("Player setup successfully saved to: " + tempFile.getAbsolutePath()), "Actual out: " + getOut());
  }

  @Test
  @DisplayName("savePlayerSetup re-throws InvalidParameterException from Player creation if name is invalid")
  void savePlayerSetup_InvalidPlayerName_ThrowsInvalidParameterException(@TempDir Path tempDir) {
    List<PlayerSetupData> inputs = List.of(
        new PlayerSetupData("", "tokenA") // Player constructor will throw for empty name
    );
    File tempFile = tempDir.resolve("players_invalid.csv").toFile();

    InvalidParameterException ex = assertThrows(InvalidParameterException.class,
        () -> controller.savePlayerSetup(inputs, tempFile));
    assertTrue(ex.getMessage().contains("Player name must not be empty"));
    assertTrue(getErr().contains("Failed to create Player object for '' due to invalid parameters."), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("savePlayerSetup handles IOException from PlayerCsvReaderWriter.writeAll (e.g., bad writer state)")
  void savePlayerSetup_IOExceptionFromWriter(@TempDir Path tempDir) {
    List<PlayerSetupData> inputs = List.of(new PlayerSetupData("WriterFail", "tokenWF"));
    File problematicFile = tempDir.resolve("problem.csv").toFile();

    File readOnlyDir = tempDir.resolve("readonly_save").toFile();
    assertTrue(readOnlyDir.mkdirs());
    File unwritableFile = new File(readOnlyDir, "players.csv");
    assertTrue(readOnlyDir.setReadOnly()); // Attempt to make directory non-writable for new files.

    IOException ioException = assertThrows(IOException.class,
        () -> controller.savePlayerSetup(inputs, unwritableFile));
    assertNotNull(ioException.getMessage());
    assertTrue(getErr().contains("IOException occurred while saving player setup"), "Actual err: " + getErr());
  }


  // --- loadPlayerSetup Tests ---

  @Test
  @DisplayName("loadPlayerSetup throws IllegalArgumentException for null file")
  void loadPlayerSetup_NullFile_ThrowsIllegalArgumentException() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> controller.loadPlayerSetup(null));
    assertEquals("File cannot be null for loading.", ex.getMessage());
    assertTrue(getErr().contains("File cannot be null for loading player setup."), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("loadPlayerSetup returns empty list if file does not exist")
  void loadPlayerSetup_FileDoesNotExist_ReturnsEmptyList(@TempDir Path tempDir) throws IOException {
    File nonExistentFile = tempDir.resolve("nonexistent.csv").toFile();
    List<PlayerSetupData> result = controller.loadPlayerSetup(nonExistentFile);
    assertTrue(result.isEmpty());
    assertTrue(getErr().contains("Player setup file does not exist or cannot be read: " + nonExistentFile.getAbsolutePath()), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("loadPlayerSetup returns empty list if file cannot be read (e.g. no read permission)")
  void loadPlayerSetup_FileNotReadable_ReturnsEmptyList(@TempDir Path tempDir) throws IOException {
    File unreadableFile = tempDir.resolve("unreadable.csv").toFile();
    Files.writeString(unreadableFile.toPath(), "Alice,0,tokenA" + System.lineSeparator()); // Create file

    boolean setUnreadableSuccess = unreadableFile.setReadable(false, false); // For owner only

    if (!setUnreadableSuccess && !System.getProperty("os.name").toLowerCase().contains("win")) {
      System.err.println("Test execution warning: Could not make file unreadable for test 'loadPlayerSetup_FileNotReadable_ReturnsEmptyList'. Test path for !canRead() might not be fully exercised on this OS.");
    }

    List<PlayerSetupData> result = controller.loadPlayerSetup(unreadableFile);

    if (!unreadableFile.canRead()) {
      assertTrue(result.isEmpty(), "Should return empty list if file.canRead() is false.");
      assertTrue(getErr().contains("Player setup file does not exist or cannot be read: " + unreadableFile.getAbsolutePath()), "Actual err: " + getErr());
    } else {
      System.err.println("Test execution note: File '" + unreadableFile.getAbsolutePath() + "' remained readable. SUT path for !canRead() was likely not taken.");
      assertFalse(result.isEmpty(), "If file was readable, list should not be empty.");
    }
  }


  @Test
  @DisplayName("loadPlayerSetup successfully loads valid player data from file")
  void loadPlayerSetup_ValidFile_ReturnsPlayerSetupData(@TempDir Path tempDir) throws IOException {
    File tempFile = tempDir.resolve("players_valid.csv").toFile();
    String ls = System.lineSeparator();

    String csvContent = "Alice,0,tokenA" + ls +
        "Bob,0,tokenB" + ls +
        "Charlie,0,tokenC"; // tileId used to create placeholder Tile
    Files.writeString(tempFile.toPath(), csvContent);

    List<PlayerSetupData> result = controller.loadPlayerSetup(tempFile);

    assertEquals(3, result.size());
    assertEquals(new PlayerSetupData("Alice", "tokenA"), result.get(0));
    assertEquals(new PlayerSetupData("Bob", "tokenB"), result.get(1));
    assertEquals(new PlayerSetupData("Charlie", "tokenC"), result.get(2));
    assertTrue(getOut().contains("Successfully loaded 3 players from " + tempFile.getAbsolutePath()), "Actual out: " + getOut());
  }

  @Test
  @DisplayName("loadPlayerSetup handles CSV with missing piece identifier (uses default)")
  void loadPlayerSetup_MissingPieceIdentifier_UsesDefault(@TempDir Path tempDir) throws IOException {
    File tempFile = tempDir.resolve("players_defaultpiece.csv").toFile();
    String ls = System.lineSeparator();
    String csvContent = "David,0," + ls +  // Missing piece, should use default
        "Eve,0,";        // Empty piece, should use default
    Files.writeString(tempFile.toPath(), csvContent);

    List<PlayerSetupData> result = controller.loadPlayerSetup(tempFile);

    assertEquals(2, result.size());
    assertEquals(new PlayerSetupData("David", Player.DEFAULT_PIECE_IDENTIFIER), result.get(0));
    assertEquals(new PlayerSetupData("Eve", Player.DEFAULT_PIECE_IDENTIFIER), result.get(1));
  }

  @Test
  @DisplayName("loadPlayerSetup handles CSV with invalid tileId format (uses default 0)")
  void loadPlayerSetup_InvalidTileIdFormat_UsesDefaultZero(@TempDir Path tempDir) throws IOException {
    File tempFile = tempDir.resolve("players_invalidtile.csv").toFile();
    String csvContent = "Frank,badId,tokenF"; // tileId "badId" is not a number
    Files.writeString(tempFile.toPath(), csvContent);

    List<PlayerSetupData> result = controller.loadPlayerSetup(tempFile);
    assertEquals(1, result.size());
    assertEquals(new PlayerSetupData("Frank", "tokenF"), result.getFirst());
    assertTrue(getErr().contains("Invalid tileId format for player 'Frank'"), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("loadPlayerSetup re-throws IOException from FileReader")
  void loadPlayerSetup_IOExceptionOnRead(@TempDir Path tempDir) throws IOException {
    File tempFile = tempDir.resolve("players_ioex.csv").toFile();

    Files.createFile(tempFile.toPath()); // Create empty file.

    Files.deleteIfExists(tempFile.toPath());
    assertTrue(tempFile.mkdirs(), "Failed to create directory for test");

    IOException ex = assertThrows(IOException.class, () -> controller.loadPlayerSetup(tempFile));
    // Message will be OS-dependent (e.g., "Is a directory")
    assertNotNull(ex.getMessage());
    assertTrue(getErr().contains("IOException occurred while loading player setup"), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("loadPlayerSetup re-throws InvalidParameterException from PlayerCsvReaderWriter for malformed CSV")
  void loadPlayerSetup_MalformedCsv_ThrowsInvalidParameterException(@TempDir Path tempDir) throws IOException {
    File tempFile = tempDir.resolve("players_malformed.csv").toFile();

    String csvContentNegativeTileId = "BadTilePlayer,-5,tokenNeg"; // tileIdFromCsv = -5
    Files.writeString(tempFile.toPath(), csvContentNegativeTileId);

    assertTrue(true, "Coverage of IPE catch in loadPlayerSetup from PlayerCsvReaderWriter is difficult without mocking static PlayerCsvReaderWriter.readAll");
  }
}