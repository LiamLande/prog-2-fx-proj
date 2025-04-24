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


  //MONEY MANAGEMENT


  public void setMoney(Integer money) {
    if (money == null) {
      throw new InvalidParameterException("Money must not be null");
    }
    if (money < 0) {
      throw new InvalidParameterException("Money must not be negative");
    }
    this.money = money;
  }

  public void increaseMoney(int amount) {
    if (money == null) {
      money = 0;
    }
    money += amount;
  }

  public void decreaseMoney(int amount) {
    if (money == null || money < amount) {
      throw new InvalidParameterException("Not enough money");
    }
    money -= amount;
  }

  public int getMoney() {
    return money != null ? money : 0;
  }

  // For utility properties
  public int getLastDiceRoll() {
    // This should be implemented to track the last dice roll
    return 0;  // Placeholder
  }

  public int getUtilitiesOwnedCount() {
    // Count utilities owned by this player
    return 0;  // Placeholder
  }
}
