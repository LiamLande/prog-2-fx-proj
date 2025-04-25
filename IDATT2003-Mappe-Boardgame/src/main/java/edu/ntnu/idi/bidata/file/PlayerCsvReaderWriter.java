package edu.ntnu.idi.bidata.file;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
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
    for (String[] row : rows) {
      if (row.length < 2) continue;
      String name = row[0].trim();
      int tileId;
      try {
        tileId = Integer.parseInt(row[1].trim());
      } catch (NumberFormatException e) {
        throw new InvalidParameterException("Invalid tileId for player " + name);
      }
      // The caller must provide a BoardGame or Board to place the player on tileId.
      // Here we simply create Player with null start (to be set later).
      Player player = new Player(name, /* placeholder: will be set */ null);
      // store tileId somewhere or use overload
      players.add(player);
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
      rows.add(new String[]{p.getName(), String.valueOf(p.getCurrentTile().getId())});
    }
    CsvUtils.writeAll(writer, rows);
  }
}