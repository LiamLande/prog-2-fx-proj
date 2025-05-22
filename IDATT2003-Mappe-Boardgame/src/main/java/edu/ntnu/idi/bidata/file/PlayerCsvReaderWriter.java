package edu.ntnu.idi.bidata.file;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.util.CsvUtils;
import edu.ntnu.idi.bidata.util.Logger; // Added Logger import

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes players to/from CSV format.
 * Each line: name,startingTileId,pieceIdentifier
 */
public class PlayerCsvReaderWriter {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private PlayerCsvReaderWriter() {
    // Prevent instantiation
  }
  /**
   * Reads players from CSV. Each line: name,startingTileId,pieceIdentifier
   *
   * @param reader The {@link Reader} to read the CSV data from.
   * @return A list of {@link Player} objects created from the CSV data.
   * @throws IOException If an I/O error occurs during reading.
   * @throws InvalidParameterException If the reader is null or if any player data is invalid.
   */
  public static List<Player> readAll(Reader reader) throws IOException {
    Logger.info("Starting to read player configurations from CSV.");
    if (reader == null) {
      Logger.error("Reader provided to readAll players is null.");
      throw new InvalidParameterException("Reader must not be null");
    }
    List<Player> players = new ArrayList<>();
    List<String[]> rows = CsvUtils.readAll(reader);
    Logger.debug("Successfully read " + rows.size() + " rows from CSV.");

    Tile placeholderStartTile;
    int rowNum = 0;

    for (String[] row : rows) {
      rowNum++;
      Logger.debug("Processing CSV row " + rowNum + ": " + String.join(",", row));
      if (row.length < 2) {
        Logger.warning("Skipping malformed CSV row " + rowNum + " (less than 2 columns): " + String.join(",", row));
        continue;
      }
      String name = row[0].trim();
      if (name.isEmpty()) {
        Logger.warning("Skipping CSV row " + rowNum + " with empty player name.");
        continue;
      }
      Logger.debug("Player name from row " + rowNum + ": '" + name + "'");

      int tileIdFromCsv;
      try {
        tileIdFromCsv = Integer.parseInt(row[1].trim());
        Logger.debug("Tile ID from row " + rowNum + " for player '" + name + "': " + tileIdFromCsv);
      } catch (NumberFormatException e) {
        Logger.warning("Invalid tileId format for player '" + name + "' in row " + rowNum + ". Using default tile ID 0. Error: " + e.getMessage());
        tileIdFromCsv = 0;
      }

      String pieceIdentifier = Player.DEFAULT_PIECE_IDENTIFIER;
      if (row.length >= 3 && row[2] != null && !row[2].trim().isEmpty()) {
        pieceIdentifier = row[2].trim();
        Logger.debug("Piece identifier from row " + rowNum + " for player '" + name + "': '" + pieceIdentifier + "'");
      } else if (row.length < 3) {
        Logger.debug("Piece identifier missing for player '" + name + "' in row " + rowNum + ". Using default: '" + pieceIdentifier + "'");
      } else {
        Logger.debug("Piece identifier was null or empty for player '" + name + "' in row " + rowNum + ". Using default: '" + pieceIdentifier + "'");
      }


      try {
        placeholderStartTile = new Tile(tileIdFromCsv);
        Logger.debug("Created placeholder tile with ID " + tileIdFromCsv + " for player '" + name + "'.");
      } catch (InvalidParameterException e) {
        Logger.error("Could not create placeholder tile for player '" + name + "' with ID " + tileIdFromCsv + ". Error: " + e.getMessage() + ". Skipping player from row " + rowNum + ".", e);
        continue;
      }

      try {
        Player player = new Player(name, placeholderStartTile, pieceIdentifier);
        players.add(player);
        Logger.debug("Successfully created and added player: '" + name + "' with piece '" + pieceIdentifier + "' starting on placeholder tile " + tileIdFromCsv + ".");
      } catch (InvalidParameterException e) {
        Logger.error("Could not create player '" + name + "'. Error: " + e.getMessage() + ". Skipping player from row " + rowNum + ".", e);
      }
    }
    Logger.info("Finished reading player configurations. Loaded " + players.size() + " players.");
    return players;
  }

  /**
   * Writes players to CSV. Each line: name,currentTileId,pieceIdentifier
   */
  public static void writeAll(Writer writer, List<Player> players) throws IOException {
    Logger.info("Starting to write " + (players != null ? players.size() : "null") + " player configurations to CSV.");
    if (writer == null) {
      Logger.error("Writer provided to writeAll players is null.");
      throw new InvalidParameterException("Writer must not be null");
    }
    if (players == null) {
      Logger.error("Players list provided to writeAll is null.");
      throw new InvalidParameterException("Players list must not be null");
    }
    List<String[]> rows = new ArrayList<>();
    for (Player p : players) {
      if (p == null) {
        Logger.warning("Encountered a null player object in the list. Skipping this player for CSV writing.");
        continue;
      }
      String tileIdStr = "0";
      if (p.getCurrentTile() != null) {
        tileIdStr = String.valueOf(p.getCurrentTile().getId());
      } else {
        Logger.warning("Player '" + p.getName() + "' has a null currentTile. Writing with default tile ID 0 for CSV.");
      }
      rows.add(new String[]{p.getName(), tileIdStr, p.getPieceIdentifier()});
      Logger.debug("Prepared CSV row for player '" + p.getName() + "': " + p.getName() + "," + tileIdStr + "," + p.getPieceIdentifier());
    }
    CsvUtils.writeAll(writer, rows);
    Logger.info("Successfully wrote " + rows.size() + " player configurations to CSV.");
  }
}