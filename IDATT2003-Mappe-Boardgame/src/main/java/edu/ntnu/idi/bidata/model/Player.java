package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;

/**
 * Represents a game player on the board.
 */
public class Player {
  private final String name;
  private Tile currentTile;
  private Integer money;
  private String pieceIdentifier;

  public static final String DEFAULT_PIECE_IDENTIFIER = "default_token";

  /**
   * Creates a player starting on the given tile with a default piece.
   * @param name must be non‐empty
   * @param start starting tile, non‐null
   */
  public Player(String name, Tile start) {
    this(name, start, DEFAULT_PIECE_IDENTIFIER, null);
  }

  /**
   * Creates a player starting on the given tile with a specified piece.
   * @param name must be non‐empty
   * @param start starting tile, non‐null
   * @param pieceIdentifier identifier for the player's piece
   */
  public Player(String name, Tile start, String pieceIdentifier) {
    this(name, start, pieceIdentifier, null);
  }


  /**
   * Creates a player starting on the given tile, with a specified piece and money.
   * This is the most comprehensive constructor.
   * @param name must be non‐empty
   * @param start starting tile, non‐null
   * @param pieceIdentifier identifier for the player's piece
   * @param money initial amount of money (can be null if not applicable)
   */
  public Player(String name, Tile start, String pieceIdentifier, Integer money) {
    if (name == null || name.isBlank()) {
      throw new InvalidParameterException("Player name must not be empty");
    }
    if (start == null) {
      throw new InvalidParameterException("Starting tile must not be null");
    }

    this.name = name;
    this.currentTile = start;
    this.pieceIdentifier = (pieceIdentifier == null || pieceIdentifier.isBlank()) ? DEFAULT_PIECE_IDENTIFIER : pieceIdentifier.trim();
    this.money = money;
  }

  public void setTile(Tile tile) {
    if (tile == null) {
      throw new InvalidParameterException("Tile must not be null");
    }
    this.currentTile = tile;
  }

  public String getName() {
    return name;
  }

  public Tile getCurrentTile() {
    return currentTile;
  }

  public void setCurrentTile(Tile current) {
    if (current == null) {
      throw new InvalidParameterException("Current tile must not be null");
    }
    this.currentTile = current;
  }

  public String getPieceIdentifier() {
    return pieceIdentifier;
  }

  public void move(int steps) {
    if (steps > 0) {
      for (int i = 0; i < steps; i++) {
        if (currentTile.getNext() == null) break;
        currentTile = currentTile.getNext();
      }
    } else if (steps < 0) {
      for (int i = 0; i < -steps; i++) {
        if (currentTile.getPrevious() == null) break;
        currentTile = currentTile.getPrevious();
      }
    }
    if (currentTile != null) {
      currentTile.land(this);
    }
  }

  public void setMoney(Integer money) {
    if (money == null) {
      this.money = money;
      return;
    }
    if (money < 0) {
      throw new InvalidParameterException("Money must not be negative");
    }
    this.money = money;
  }

  public void increaseMoney(int amount) {
    if (this.money == null) {
      this.money = 0;
    }
    this.money += amount;
  }

  public void decreaseMoney(int amount) {
    if (this.money == null || this.money < amount) {
      throw new InvalidParameterException("Not enough money or money not initialized.");
    }
    this.money -= amount;
  }

  public int getMoney() {
    return money != null ? money : 0;
  }

  public int getLastDiceRoll() {
    return 0;
  }

  public int getUtilitiesOwnedCount() {
    return 0;
  }
}