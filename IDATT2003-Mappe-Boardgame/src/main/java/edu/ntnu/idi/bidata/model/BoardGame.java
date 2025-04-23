// BoardGame.java
package edu.ntnu.idi.bidata.model;


import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.service.GameService;
import edu.ntnu.idi.bidata.service.SnakesLaddersService;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade for game setup and play. Delegates to a GameService implementation.
 */
public class BoardGame {
  private Board board;
  private Dice dice;
  private final List<Player> players = new ArrayList<>();
  private GameService service;

  /**
   * Must be called after setting board and dice, before adding players.
   */
  public void init() {
    // Default to Snakes & Ladders if not set
    if (service == null) {
      service = new SnakesLaddersService();
    }
    service.setup(this);
  }

  /**
   * Sets the board configuration. Used by factories.
   */
  public void setBoard(Board board) {
    if (board == null) {
      throw new InvalidParameterException("Board cannot be null");
    }
    this.board = board;
  }

  /**
   * Sets the dice to use (e.g., number of dice).
   */
  public void setDice(Dice dice) {
    if (dice == null) {
      throw new InvalidParameterException("Dice cannot be null");
    }
    this.dice = dice;
  }

  /**
   * Adds a player to this game. init() must have been called.
   */
  public void addPlayer(Player player) {
    if (player == null) {
      throw new InvalidParameterException("Player cannot be null");
    }
    players.add(player);
  }

  /**
   * Plays one round: each player rolls once.
   * @return list of roll outcomes in order of players
   */
  public List<Integer> playOneRound() {
    if (service == null) {
      throw new IllegalStateException("Game not initialized");
    }
    return service.playOneRound(this);
  }

  /**
   * Checks if the game has finished (winner found).
   */
  public boolean isFinished() {
    if (service == null) {
      throw new IllegalStateException("Game not initialized");
    }
    return service.isFinished(this);
  }

  /**
   * Returns the winner when finished, or null if none.
   */
  public Player getWinner() {
    if (service == null) {
      throw new IllegalStateException("Game not initialized");
    }
    return service.getWinner(this);
  }

  // Getters for GameService to use
  public Board getBoard() { return board; }
  public Dice getDice() { return dice; }
  public List<Player> getPlayers() { return new ArrayList<>(players); }

  /**
   * Allows injection of alternative game logic (e.g., monopoly service).
   */
  public void setGameService(GameService service) {
    this.service = service;
  }
}
