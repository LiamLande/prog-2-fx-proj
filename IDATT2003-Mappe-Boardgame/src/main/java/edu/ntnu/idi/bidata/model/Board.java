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
   * Adds a tile to the board. Id must be unique.
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

  /**
   * Given a current tile and a dice‐roll, returns the next tile:
   * - computes (current.id + roll)
   * - if that id is past the end, stays on the same tile
   * - otherwise looks up the tile, and then follows any snake/ladder
   *   via tile.getNext() if present
   *
   * @throws IllegalArgumentException if the target tile id exists but isn't on the board
   */
  public Tile getDestination(Tile current, int roll) {
    if (current == null) {
      throw new InvalidParameterException("Current tile cannot be null");
    }
    int targetId = current.getId() + roll;

    // If roll would take you past the final square, you stay put
    if (targetId > maxId) {
      return current;
    }

    Tile target = tiles.get(targetId);
    if (target == null) {
      throw new IllegalArgumentException(
          "No tile with id=" + targetId + " on this board");
    }

    // If there's a snake or ladder (i.e. target.getNext() != null), follow it
    return (target.getNext() != null)
        ? target.getNext()
        : target;
  }
}
