package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the game board: manages tiles by their id,
 * knows its “start” square, and can compute move destinations
 * (including snakes/ladders).
 */
public class Board {
  private final Map<Integer, Tile> tiles = new LinkedHashMap<>();
  private int maxId = 0;

  /**
   * Adds a tile to the board. ID must be unique.
   */
  public void addTile(Tile tile) {
    if (tile == null) {
      throw new InvalidParameterException("Tile must not be null");
    }
    int id = tile.getId();
    if (tiles.containsKey(id)) {
      throw new InvalidParameterException("Tile ID " + id + " already exists");
    }
    tiles.put(id, tile);
    if (id > maxId) {
      maxId = id;
    }
  }

  /**
   * Retrieves the tile by id, or null if not present.
   */
  public Tile getTile(int id) {
    return tiles.get(id);
  }

  /**
   * Returns a read-only view of all tiles.
   */
  public Map<Integer, Tile> getTiles() {
    return Collections.unmodifiableMap(tiles);
  }

  /**
   * The “start” tile of the board (lowest id).
   * @throws IllegalStateException if no tiles have been added
   */
  public Tile getStart() {
    if (tiles.isEmpty()) {
      throw new IllegalStateException("Board is empty; no start tile");
    }
    // LinkedHashMap preserves insertion order, but to be safe:
    int firstId = tiles.keySet().stream().min(Integer::compareTo).get();
    return tiles.get(firstId);
  }
}
