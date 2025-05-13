package edu.ntnu.idi.bidata.file;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile; // Import Tile
import edu.ntnu.idi.bidata.util.CsvUtils;

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
   * Reads players from the given CSV Reader.
   * Expects each row to have up to three columns: name, tileId, pieceIdentifier.
   * pieceIdentifier is optional for backward compatibility.
   */
  public static List<Player> readAll(Reader reader) throws IOException {
    if (reader == null) {
      throw new InvalidParameterException("Reader must not be null");
    }
    List<Player> players = new ArrayList<>();
    List<String[]> rows = CsvUtils.readAll(reader);

    Tile placeholderStartTile;

    for (String[] row : rows) {
      if (row.length < 2) { // Minimum 2 columns for name and tileId
        System.err.println("Skipping malformed CSV row (less than 2 columns): " + String.join(",", row));
        continue;
      }
      String name = row[0].trim();
      if (name.isEmpty()) {
        System.err.println("Skipping CSV row with empty player name.");
        continue;
      }

      int tileIdFromCsv;
      try {
        tileIdFromCsv = Integer.parseInt(row[1].trim());
      } catch (NumberFormatException e) {
        System.err.println("Invalid tileId format for player '" + name + "'. Using default for placeholder. Error: " + e.getMessage());
        tileIdFromCsv = 0; // Default if parsing fails
      }

      // Extract piece identifier (new) - column 3
      String pieceIdentifier = Player.DEFAULT_PIECE_IDENTIFIER; // Default from Player class
      if (row.length >= 3 && row[2] != null && !row[2].trim().isEmpty()) {
        pieceIdentifier = row[2].trim();
      } else if (row.length < 3) {
        System.err.println("Piece identifier missing for player '" + name + "'. Using default: " + pieceIdentifier);
      }


      try {
        placeholderStartTile = new Tile(tileIdFromCsv);
      } catch (InvalidParameterException e) {
        System.err.println("Could not create placeholder tile for player '" + name + "' with ID " + tileIdFromCsv + ". Error: " + e.getMessage() + ". Skipping player.");
        continue;
      }

      try {
        // Use the constructor that accepts pieceIdentifier
        Player player = new Player(name, placeholderStartTile, pieceIdentifier);
        players.add(player);
      } catch (InvalidParameterException e) {
        System.err.println("Could not create player '" + name + "'. Error: " + e.getMessage() + ". Skipping player.");
      }
    }
    return players;
  }

  /**
   * Writes players to CSV. Each line: name,currentTileId,pieceIdentifier
   */
  public static void writeAll(Writer writer, List<Player> players) throws IOException {
    if (writer == null) {
      throw new InvalidParameterException("Writer must not be null");
    }
    if (players == null) {
      throw new InvalidParameterException("Players list must not be null");
    }
    List<String[]> rows = new ArrayList<>();
    for (Player p : players) {
      String tileIdStr = "0";
      if (p.getCurrentTile() != null) {
        tileIdStr = String.valueOf(p.getCurrentTile().getId());
      } else {
        System.err.println("Warning: Player '" + p.getName() + "' has a null currentTile. Writing with default tile ID 0.");
      }
      // Add piece identifier to the row
      rows.add(new String[]{p.getName(), tileIdStr, p.getPieceIdentifier()});
    }
    CsvUtils.writeAll(writer, rows);
  }
}