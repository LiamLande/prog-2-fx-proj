package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;

/**
 * Represents a game player on the board.
 */
public class Player {
  private final String name;
  private Tile current;
  private Integer money;

  /**
   * Creates a player starting on the given tile.
   * @param name must be non‐empty
   * @param start starting tile, non‐null
   */
  public Player(String name, Tile start) {
    if (name == null || name.isBlank()) {
      throw new InvalidParameterException("Player name must not be empty");
    }
    if (start == null) {
      throw new InvalidParameterException("Starting tile must not be null");
    }
    this.name = name;
    this.current = start;
  }

  /**
   * Creates a player starting on the given tile.
   * @param name must be non‐empty
   * @param start starting tile, non‐null
   */
  public Player(String name, Tile start, Integer money) {
    if (name == null || name.isBlank()) {
      throw new InvalidParameterException("Player name must not be empty");
    }
    if (start == null) {
      throw new InvalidParameterException("Starting tile must not be null");
    }
    if (money == null) {
      throw new InvalidParameterException("Starting Money must not be null");
    }
    this.name = name;
    this.current = start;
    this.money = money;
  }


  public String getName() {
    return name;
  }

  public Tile getCurrent() {
    return current;
  }

  /**
   * Moves the player by the given steps: positive forward, negative backward.
   */
  public void move(int steps) {
    if (steps > 0) {
      for (int i = 0; i < steps; i++) {
        if (current.getNext() == null) break;
        current = current.getNext();
      }
    } else if (steps < 0) {
      for (int i = 0; i < -steps; i++) {
        if (current.getPrevious() == null) break;
        current = current.getPrevious();
      }
    }
    // trigger any special action
    current.land(this);
  }
}
