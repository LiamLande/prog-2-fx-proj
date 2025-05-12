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
 * Each line: name,startingTileId
 */
public class PlayerCsvReaderWriter {

  /**
   * Reads players from the given CSV Reader.
   * Expects each row to have two columns: name, tileId.
   */
  public static List<Player> readAll(Reader reader) throws IOException {
    if (reader == null) {
      throw new InvalidParameterException("Reader must not be null");
    }
    List<Player> players = new ArrayList<>();
    List<String[]> rows = CsvUtils.readAll(reader);

    // Create a single, reusable placeholder start tile for all players read.
    // The tile ID from the CSV (row[1]) is parsed but not directly used to
    // create a complex Tile object linked to a board here, as the purpose
    // in the setup scene is primarily to get names.
    // We still need *a* non-null tile for the Player constructor.
    Tile placeholderStartTile = null; // Initialize to null

    for (String[] row : rows) {
      if (row.length < 2) {
        System.err.println("Skipping malformed CSV row (less than 2 columns): " + String.join(",", row));
        continue;
      }
      String name = row[0].trim();
      if (name.isEmpty()) {
        System.err.println("Skipping CSV row with empty player name.");
        continue;
      }

      int tileIdFromCsv; // We still parse it, though not used to create a complex tile here.
      try {
        tileIdFromCsv = Integer.parseInt(row[1].trim());
      } catch (NumberFormatException e) {
        // If tileId is invalid, we could skip or throw a more specific error.
        // For robustness in setup, let's log and continue, or use a default ID for the placeholder.
        System.err.println("Invalid tileId format for player '" + name + "'. Using default for placeholder. Error: " + e.getMessage());
        tileIdFromCsv = 0; // Default if parsing fails for the placeholder's ID
      }

      // Ensure placeholderStartTile is created (once is enough, or per player if ID matters for placeholder)
      // For simplicity, let's use the ID from the CSV (or default 0 if parse failed)
      // for the placeholder tile, even though its connections (next/prev) won't be set.
      try {
        placeholderStartTile = new Tile(tileIdFromCsv);
      } catch (InvalidParameterException e) {
        // This might happen if new Tile(id) itself throws for some IDs, e.g., negative.
        // The Tile constructor check is "id < 0".
        System.err.println("Could not create placeholder tile for player '" + name + "' with ID " + tileIdFromCsv + ". Error: " + e.getMessage() + ". Skipping player.");
        continue; // Skip this player if placeholder tile can't be made
      }


      // Create Player with the non-null placeholder start tile.
      // The actual tileId from the CSV is not critical for the PlayerSetupScene's use case
      // which is just to display names. The game loading logic would handle actual tile placement.
      try {
        Player player = new Player(name, placeholderStartTile);
        // The tileIdFromCsv could be stored as an attribute on Player if needed later
        // before actual game board placement, e.g., player.setInitialTileId(tileIdFromCsv);
        players.add(player);
      } catch (InvalidParameterException e) {
        // Catch exception from Player constructor (e.g. if name is now blank after trim, though checked above)
        System.err.println("Could not create player '" + name + "'. Error: " + e.getMessage() + ". Skipping player.");
      }
    }
    return players;
  }

  /**
   * Writes players to CSV. Each line: name,currentTileId
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
      if (p.getCurrentTile() == null) {
        // This should not happen if players are correctly initialized.
        // Handle defensively: write a default tile ID or skip.
        System.err.println("Warning: Player '" + p.getName() + "' has a null currentTile. Writing with default tile ID 0.");
        rows.add(new String[]{p.getName(), "0"});
      } else {
        rows.add(new String[]{p.getName(), String.valueOf(p.getCurrentTile().getId())});
      }
    }
    CsvUtils.writeAll(writer, rows);
  }
}