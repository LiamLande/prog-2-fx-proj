package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.service.GameService;
import edu.ntnu.idi.bidata.service.SnakesLaddersService;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade for game setup and play. Delegates to a GameService implementation and notifies observers.
 */
public class BoardGame {
  private Board board;
  private Dice dice;
  private final List<Player> players = new ArrayList<>();
  private GameService service;

  // Observer registry
  private final List<BoardGameObserver> observers = new ArrayList<>();

  /**
   * Registers a new observer.
   */
  public void addObserver(BoardGameObserver observer) {
    if (observer == null) {
      throw new InvalidParameterException("Observer cannot be null");
    }
    observers.add(observer);
  }

  /**
   * Unregisters an existing observer.
   */
  public void removeObserver(BoardGameObserver observer) {
    observers.remove(observer);
  }

  /**
   * Must be called after setting board and dice, before adding players.
   */
  public void init() {
    if (service == null) {
      service = new SnakesLaddersService();
    }
    service.setup(this);
    // Notify observers that the game is starting
    for (BoardGameObserver obs : observers) {
      obs.onGameStart(getPlayers());
    }
  }

  /**
   * Plays one round: each player rolls once.
   * @return list of roll outcomes in order of players
   */
  public List<Integer> playOneRound() {
    if (service == null) {
      throw new IllegalStateException("Game not initialized");
    }
    List<Integer> rolls = service.playOneRound(this);
    // Notify observers of the round
    for (BoardGameObserver obs : observers) {
      obs.onRoundPlayed(rolls, getPlayers());
    }
    // If game ended, notify observers
    if (isFinished()) {
      Player winner = getWinner();
      for (BoardGameObserver obs : observers) {
        obs.onGameOver(winner);
      }
    }
    return rolls;
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

  // Existing setters and getters...
  public void setBoard(Board board) {
    if (board == null) {
      throw new InvalidParameterException("Board cannot be null");
    }
    this.board = board;
  }

  public void setDice(Dice dice) {
    if (dice == null) {
      throw new InvalidParameterException("Dice cannot be null");
    }
    this.dice = dice;
  }

  public void addPlayer(Player player) {
    if (player == null) {
      throw new InvalidParameterException("Player cannot be null");
    }
    players.add(player);
  }

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
