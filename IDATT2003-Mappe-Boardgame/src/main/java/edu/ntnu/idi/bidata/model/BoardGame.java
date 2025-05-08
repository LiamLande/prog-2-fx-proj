package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.service.GameService;
import edu.ntnu.idi.bidata.service.SnakesLaddersService;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade for game setup and play.
 * Delegates to a GameService implementation and notifies observers.
 */
public class BoardGame {
  private Board board;
  private Dice dice;
  private final List<Player> players = new ArrayList<>();
  private GameService service;
  private final List<BoardGameObserver> observers = new ArrayList<>();
  private boolean gameInitialized = false;

  // --- Observer registration ---
  public void addObserver(BoardGameObserver observer) {
    if (observer == null) {
      throw new InvalidParameterException("Observer cannot be null");
    }
    observers.add(observer);
  }
  public void removeObserver(BoardGameObserver observer) {
    observers.remove(observer);
  }

  // --- Initialization ---
  /**
   * Call *after* setting board, dice, and adding players,
   * to wire in your GameService and fire the start‐game event.
   */
  public void init() {
    // 1) validate that everything’s been wired
    if (board == null) {
      throw new IllegalStateException("Board must be set before init()");
    }
    if (dice == null) {
      throw new IllegalStateException("Dice must be set before init()");
    }
    if (players.isEmpty()) {
      throw new IllegalStateException("At least one player must be added before init()");
    }

    // 2) let the service do its setup
    service.setup(this);
    this.gameInitialized = true; // Mark as initialized AFTER service.setup() completes
    // 3) notify observers
    notifyGameStart();
  }

  // --- Full‐round play ---
  /**
   * Each player takes exactly one turn this round.
   * @return the raw dice‐rolls in player-order
   */
  public List<Integer> playOneRound() {
    requireInitialized();
    List<Integer> rolls = service.playOneRound(this);
    notifyRoundPlayed(rolls);
    if (isFinished()) {
      notifyGameOver(getWinner());
    }
    return rolls;
  }

  /**
   * Plays exactly one roll/move for the given player.
   */
  public void playTurn(Player player) {
    requireInitialized();
    if (!players.contains(player)) {
      throw new IllegalArgumentException("Player is not part of this game");
    }

    // let the service do the roll & move
    int roll = service.playTurn(this, player);

    notifyRoundPlayed(List.of(roll));
    if (isFinished()) {
      notifyGameOver(getWinner());
    }
  }

  // --- Queries delegated to the service ---
  public boolean isFinished() {
    requireInitialized();
    return service.isFinished(this);
  }

  /**
   * Checks if the game has been initialized.
   * @return true if init() has been successfully called and completed, false otherwise.
   */
  public boolean isGameStarted() {
    return this.gameInitialized;
  }

  public Player getWinner() {
    requireInitialized();
    return service.getWinner(this);
  }

  // --- Wiring: board, dice, players, service injection ---
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

  public void setGameService(GameService service) {
    if (service == null) {
      throw new InvalidParameterException("GameService cannot be null");
    }
    this.service = service;
  }

  // --- Simple getters ---
  public Board getBoard() { return board; }
  public Dice getDice()   { return dice; }
  public List<Player> getPlayers() {
    // defensive copy
    return new ArrayList<>(players);
  }

  /**
   * Gets the current player whose turn it is.
   * Delegated to the GameService.
   * @return The current Player, or null if not applicable or game not started.
   */
  public Player getCurrentPlayer() {
    if (!isGameStarted()) {
      return null;
    }
    // requireInitialized(); // or this, if you prefer to throw an exception
    if (service == null) { // Should be caught by requireInitialized or isGameStarted
      throw new IllegalStateException("GameService not available, game might not be fully initialized.");
    }
    return service.getCurrentPlayer(this);
  }

  // --- Internals & notification helpers ---
  private void requireInitialized() {
    if (service == null) {
      throw new IllegalStateException("Game not initialized; call init() first");
    }
  }

  private void notifyGameStart() {
    for (var obs : observers) {
      obs.onGameStart(getPlayers());
    }
  }

  private void notifyRoundPlayed(List<Integer> rolls) {
    for (var obs : observers) {
      obs.onRoundPlayed(rolls, getPlayers());
    }
  }

  private void notifyGameOver(Player winner) {
    for (var obs : observers) {
      obs.onGameOver(winner);
    }
  }
}
