package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents the game board: manages tiles by their id.
 */
public class Board {
  private final Map<Integer, Tile> tiles = new HashMap<>();

  /**
   * Adds a tile to the board. Id must be unique.
   */
  public void addTile(Tile tile) {
    if (tile == null) {
      throw new InvalidParameterException("Tile must not be null");
    }
    tiles.put(tile.getId(), tile);
  }

  /**
   * Retrieves the tile by id, or null if not present.
   */
  public Tile getTile(int id) {
    return tiles.get(id);
  }
}