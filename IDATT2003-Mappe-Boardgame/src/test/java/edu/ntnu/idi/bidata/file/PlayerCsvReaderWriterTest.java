package edu.ntnu.idi.bidata.file;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.util.CsvUtils;
import edu.ntnu.idi.bidata.util.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerCsvReaderWriterTest {

    @Mock
    Player mockPlayer1;

    @Mock
    Player mockPlayer2;

    @Mock
    Tile mockTile1;

    @Mock
    Tile mockTile2;


    @BeforeEach
    void setUp() {
        // Lenient stubs for player mocks for general use
        lenient().when(mockPlayer1.getName()).thenReturn("Alice");
        lenient().when(mockPlayer1.getCurrentTile()).thenReturn(mockTile1);
        lenient().when(mockTile1.getId()).thenReturn(10);
        lenient().when(mockPlayer1.getPieceIdentifier()).thenReturn("Car");

        lenient().when(mockPlayer2.getName()).thenReturn("Bob");
        lenient().when(mockPlayer2.getCurrentTile()).thenReturn(mockTile2);
        lenient().when(mockTile2.getId()).thenReturn(20);
        lenient().when(mockPlayer2.getPieceIdentifier()).thenReturn("Hat");
    }

    @Test
    void readAll_nullReader_throwsInvalidParameterException() {
        try (MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> PlayerCsvReaderWriter.readAll(null));
            assertEquals("Reader must not be null", exception.getMessage());
            mockedLogger.verify(() -> Logger.error(eq("Reader provided to readAll players is null.")));
        }
    }

    @Test
    void readAll_emptyCsv_returnsEmptyList() throws IOException {
        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            when(CsvUtils.readAll(any(Reader.class))).thenReturn(Collections.emptyList());
            StringReader emptyReader = new StringReader("");

            List<Player> players = PlayerCsvReaderWriter.readAll(emptyReader);

            assertTrue(players.isEmpty());
            mockedLogger.verify(() -> Logger.info(eq("Starting to read player configurations from CSV.")));
            mockedLogger.verify(() -> Logger.debug(eq("Successfully read 0 rows from CSV.")));
            mockedLogger.verify(() -> Logger.info(eq("Finished reading player configurations. Loaded 0 players.")));
        }
    }

    @Test
    void readAll_validCsv_twoColumns_createsPlayersWithDefaultPiece() throws IOException {
        String csvData = "PlayerOne,1\nPlayerTwo,2";
        StringReader reader = new StringReader(csvData);

        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(new String[]{"PlayerOne", "1"});
            csvRows.add(new String[]{"PlayerTwo", "2"});
            when(CsvUtils.readAll(reader)).thenReturn(csvRows);

            List<Player> players = PlayerCsvReaderWriter.readAll(reader);

            assertEquals(2, players.size());
            assertEquals("PlayerOne", players.getFirst().getName());
            assertEquals(1, players.get(0).getCurrentTile().getId());
            assertEquals(Player.DEFAULT_PIECE_IDENTIFIER, players.get(0).getPieceIdentifier());

            assertEquals("PlayerTwo", players.get(1).getName());
            assertEquals(2, players.get(1).getCurrentTile().getId());
            assertEquals(Player.DEFAULT_PIECE_IDENTIFIER, players.get(1).getPieceIdentifier());

            mockedLogger.verify(() -> Logger.debug(contains("Piece identifier missing for player 'PlayerOne'")));
            mockedLogger.verify(() -> Logger.debug(contains("Piece identifier missing for player 'PlayerTwo'")));
        }
    }

    @Test
    void readAll_validCsv_threeColumns_createsPlayers() throws IOException {
        String csvData = "PlayerOne,1,Car\nPlayerTwo,2,Ship";
        StringReader reader = new StringReader(csvData);

        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(new String[]{"PlayerOne", "1", "Car"});
            csvRows.add(new String[]{"PlayerTwo", "2", "Ship"});
            when(CsvUtils.readAll(reader)).thenReturn(csvRows);


            List<Player> players = PlayerCsvReaderWriter.readAll(reader);

            assertEquals(2, players.size());
            assertEquals("PlayerOne", players.getFirst().getName());
            assertEquals(1, players.get(0).getCurrentTile().getId());
            assertEquals("Car", players.get(0).getPieceIdentifier());

            assertEquals("PlayerTwo", players.get(1).getName());
            assertEquals(2, players.get(1).getCurrentTile().getId());
            assertEquals("Ship", players.get(1).getPieceIdentifier());
            mockedLogger.verify(() -> Logger.debug(eq("Piece identifier from row 1 for player 'PlayerOne': 'Car'")));
        }
    }

    @Test
    void readAll_csvWithEmptyPieceIdentifier_usesDefault() throws IOException {
        String csvData = "PlayerOne,1, \nPlayerTwo,2,"; // Note the space for first, empty for second
        StringReader reader = new StringReader(csvData);

        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(new String[]{"PlayerOne", "1", " "});
            csvRows.add(new String[]{"PlayerTwo", "2", ""});
            when(CsvUtils.readAll(reader)).thenReturn(csvRows);

            List<Player> players = PlayerCsvReaderWriter.readAll(reader);

            assertEquals(2, players.size());
            assertEquals(Player.DEFAULT_PIECE_IDENTIFIER, players.get(0).getPieceIdentifier());
            assertEquals(Player.DEFAULT_PIECE_IDENTIFIER, players.get(1).getPieceIdentifier());
            mockedLogger.verify(() -> Logger.debug(contains("Piece identifier was null or empty for player 'PlayerOne'")));
            mockedLogger.verify(() -> Logger.debug(contains("Piece identifier was null or empty for player 'PlayerTwo'")));
        }
    }


    @Test
    void readAll_malformedRow_lessThanTwoColumns_skipsRow() throws IOException {
        String csvData = "PlayerOne\nPlayerTwo,2,Ship"; // First row malformed
        StringReader reader = new StringReader(csvData);

        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(new String[]{"PlayerOne"});
            csvRows.add(new String[]{"PlayerTwo", "2", "Ship"});
            when(CsvUtils.readAll(reader)).thenReturn(csvRows);

            List<Player> players = PlayerCsvReaderWriter.readAll(reader);

            assertEquals(1, players.size()); // Only PlayerTwo should be loaded
            assertEquals("PlayerTwo", players.getFirst().getName());
            mockedLogger.verify(() -> Logger.warning(contains("Skipping malformed CSV row 1")));
        }
    }

    @Test
    void readAll_emptyPlayerName_skipsRow() throws IOException {
        String csvData = " ,1,Car\nPlayerTwo,2,Ship"; // First row empty name
        StringReader reader = new StringReader(csvData);
        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(new String[]{" ", "1", "Car"});
            csvRows.add(new String[]{"PlayerTwo", "2", "Ship"});
            when(CsvUtils.readAll(reader)).thenReturn(csvRows);


            List<Player> players = PlayerCsvReaderWriter.readAll(reader);

            assertEquals(1, players.size()); // Only PlayerTwo
            assertEquals("PlayerTwo", players.getFirst().getName());
            mockedLogger.verify(() -> Logger.warning(contains("Skipping CSV row 1 with empty player name.")));
        }
    }

    @Test
    void readAll_invalidTileIdFormat_usesDefaultTileIdZero() throws IOException {
        String csvData = "PlayerOne,abc,Car"; // abc is not a number
        StringReader reader = new StringReader(csvData);
        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(new String[]{"PlayerOne", "abc", "Car"});
            when(CsvUtils.readAll(reader)).thenReturn(csvRows);

            List<Player> players = PlayerCsvReaderWriter.readAll(reader);

            assertEquals(1, players.size());
            assertEquals("PlayerOne", players.getFirst().getName());
            assertEquals(0, players.getFirst().getCurrentTile().getId()); // Default ID
            mockedLogger.verify(() -> Logger.warning(contains("Invalid tileId format for player 'PlayerOne'")));
        }
    }

    @Test
    void readAll_tileCreationFails_skipsPlayer() throws IOException {
        String csvData = "ProblemPlayer,1,ProblemPiece";
        StringReader reader = new StringReader(csvData);

        String csvDataForPlayerFail = "PlayerOne,1,Good\nBadPlayerName,2,Bad"; // Assume BadPlayerName makes Player constructor fail
        StringReader readerPlayerFail = new StringReader(csvDataForPlayerFail);

        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(new String[]{"PlayerOne", "1", "Good"});
            csvRows.add(new String[]{"BadPlayerName", "2", "Piece"});
            when(CsvUtils.readAll(readerPlayerFail)).thenReturn(csvRows);

            String csvTileFail = "TileFailPlayer,-99,Piece";
            StringReader readerTileFail = new StringReader(csvTileFail);
            List<String[]> csvRowsTileFail = Collections.singletonList((new String[]{"TileFailPlayer", "-99", "Piece"}));
            when(CsvUtils.readAll(readerTileFail)).thenReturn(csvRowsTileFail);

            List<Player> playersFromTileFail = PlayerCsvReaderWriter.readAll(readerTileFail);
            assertTrue(playersFromTileFail.isEmpty());
            mockedLogger.verify(() -> Logger.error(contains("Could not create placeholder tile for player 'TileFailPlayer' with ID -99."), any(InvalidParameterException.class)));
        }
    }


    @Test
    void writeAll_nullWriter_throwsInvalidParameterException() {
        try (MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> {
                PlayerCsvReaderWriter.writeAll(null, new ArrayList<>());
            });
            assertEquals("Writer must not be null", exception.getMessage());
            mockedLogger.verify(() -> Logger.error(eq("Writer provided to writeAll players is null.")));
        }
    }

    @Test
    void writeAll_nullPlayersList_throwsInvalidParameterException() {
        try (MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            StringWriter sw = new StringWriter();
            InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> {
                PlayerCsvReaderWriter.writeAll(sw, null);
            });
            assertEquals("Players list must not be null", exception.getMessage());
            mockedLogger.verify(() -> Logger.error(eq("Players list provided to writeAll is null.")));
        }
    }

    @Test
    void writeAll_emptyPlayersList_writesNothing() throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            PlayerCsvReaderWriter.writeAll(stringWriter, Collections.emptyList());

            mockedCsvUtils.verify(() -> CsvUtils.writeAll(eq(stringWriter), eq(Collections.emptyList())));
            assertEquals("", stringWriter.toString()); // Output should be empty
            mockedLogger.verify(() -> Logger.info(eq("Successfully wrote 0 player configurations to CSV.")));
        }
    }

    @Test
    void writeAll_validPlayers_writesToCsv() throws IOException {
        StringWriter stringWriter = new StringWriter();
        List<Player> players = Arrays.asList(mockPlayer1, mockPlayer2);

        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            PlayerCsvReaderWriter.writeAll(stringWriter, players);

            List<String[]> expectedRows = new ArrayList<>();
            expectedRows.add(new String[]{"Alice", "10", "Car"});
            expectedRows.add(new String[]{"Bob", "20", "Hat"});

            // Verify CsvUtils.writeAll was called with the correct writer and data
            mockedCsvUtils.verify(() -> CsvUtils.writeAll(eq(stringWriter), argThat(list ->
                    list.size() == 2 &&
                            Arrays.equals(list.getFirst(), expectedRows.getFirst()) &&
                            Arrays.equals(list.get(1), expectedRows.get(1))
            )));
            mockedLogger.verify(() -> Logger.debug(eq("Prepared CSV row for player 'Alice': Alice,10,Car")));
            mockedLogger.verify(() -> Logger.debug(eq("Prepared CSV row for player 'Bob': Bob,20,Hat")));
            mockedLogger.verify(() -> Logger.info(eq("Successfully wrote 2 player configurations to CSV.")));
        }
    }

    @Test
    void writeAll_playerWithNullCurrentTile_writesDefaultTileIdZero() throws IOException {
        StringWriter stringWriter = new StringWriter();
        when(mockPlayer1.getCurrentTile()).thenReturn(null); // Player with null tile
        List<Player> players = Collections.singletonList(mockPlayer1);

        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            PlayerCsvReaderWriter.writeAll(stringWriter, players);

            List<String[]> expectedRows = new ArrayList<>();
            expectedRows.add(new String[]{"Alice", "0", "Car"}); // Default tile ID "0"

            mockedCsvUtils.verify(() -> CsvUtils.writeAll(eq(stringWriter), argThat(list ->
                    list.size() == 1 &&
                            Arrays.equals(list.getFirst(), expectedRows.getFirst())
            )));
            mockedLogger.verify(() -> Logger.warning(eq("Player 'Alice' has a null currentTile. Writing with default tile ID 0 for CSV.")));
        }
    }

    @Test
    void writeAll_listContainsNullPlayer_skipsNullPlayer() throws IOException {
        StringWriter stringWriter = new StringWriter();
        List<Player> players = new ArrayList<>();
        players.add(mockPlayer1);
        players.add(null); // Null player in the list
        players.add(mockPlayer2);

        try (MockedStatic<CsvUtils> mockedCsvUtils = Mockito.mockStatic(CsvUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            PlayerCsvReaderWriter.writeAll(stringWriter, players);

            List<String[]> expectedRows = new ArrayList<>();
            expectedRows.add(new String[]{"Alice", "10", "Car"});
            expectedRows.add(new String[]{"Bob", "20", "Hat"});

            // CsvUtils.writeAll should be called with rows for non-null players only
            mockedCsvUtils.verify(() -> CsvUtils.writeAll(eq(stringWriter), argThat(list ->
                    list.size() == 2 && // Only 2 non-null players
                            Arrays.equals(list.getFirst(), expectedRows.getFirst()) &&
                            Arrays.equals(list.get(1), expectedRows.get(1))
            )));
            mockedLogger.verify(() -> Logger.warning(eq("Encountered a null player object in the list. Skipping this player for CSV writing.")));
            mockedLogger.verify(() -> Logger.info(eq("Successfully wrote 2 player configurations to CSV.")));
        }
    }
}